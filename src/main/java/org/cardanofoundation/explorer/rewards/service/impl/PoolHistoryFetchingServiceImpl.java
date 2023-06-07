package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jdbc.JDBCPoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jdbc.JDBCPoolHistoryRepository;
import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolHistoryFetchingServiceImpl implements PoolHistoryFetchingService {

  final KoiosClient koiosClient;
  final EpochRepository epochRepository;
  final PoolHistoryCheckpointRepository poolHistoryCheckpointRepository;
  final JDBCPoolHistoryRepository jdbcPoolHistoryRepository;
  final JDBCPoolHistoryCheckpointRepository jdbcPoolHistoryCheckpointRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(
      String poolId) throws ApiException {
    var currentEpoch = epochRepository.findMaxEpoch();
    int smallerCurrentEpoch = Math.min(currentEpoch, getCurrentEpochInKoios());

    var dataFromKoios = getPoolHistoryList(poolId);
    var poolHistoryCheckpoint = poolHistoryCheckpointRepository.findByView(poolId);

    Map<Integer, PoolHistory> poolHistoryList =
        dataFromKoios.stream().map(poolHistory -> PoolHistory.builder()
            .epochNo(poolHistory.getEpochNo())
            .activeStake(poolHistory.getActiveStake())
            .activeStakePct(poolHistory.getActiveStakePct())
            .blockCnt(poolHistory.getBlockCnt())
            .poolFees(poolHistory.getPoolFees())
            .delegatorCnt(poolHistory.getDelegatorCnt())
            .delegRewards(poolHistory.getDelegRewards())
            .epochRos(poolHistory.getEpochRos())
            .fixedCost(poolHistory.getFixedCost())
            .margin(poolHistory.getMargin())
            .saturationPct(poolHistory.getSaturationPct())
            .poolId(poolId)
            .build()).collect(
            Collectors.toMap(PoolHistory::getEpochNo, Function.identity()));

    var poolHistoryCheck = poolHistoryList.get(smallerCurrentEpoch - 2);
    boolean checkEarnedReward =
        poolHistoryCheck == null || !poolHistoryCheck.getPoolFees().equals("0")
            || !poolHistoryCheck.getDelegRewards().equals("0")
            || poolHistoryCheck.getEpochRos() != 0.0;

    if (poolHistoryCheckpoint.isPresent()) {
      jdbcPoolHistoryRepository.saveAll(poolHistoryList.values().stream().filter(
          poolHistory -> poolHistory.getEpochNo() > poolHistoryCheckpoint.get()
              .getEpochCheckpoint() - 2).collect(Collectors.toList()));

      var checkpoint = poolHistoryCheckpoint.get();
      checkpoint.setEpochCheckpoint(smallerCurrentEpoch - 1);
      checkpoint.setEarnedReward(checkEarnedReward ? Boolean.TRUE : Boolean.FALSE);
      jdbcPoolHistoryCheckpointRepository.saveAll(List.of(checkpoint));
    } else {
      jdbcPoolHistoryRepository.saveAll(poolHistoryList.values().stream().toList());
      var checkpoint = PoolHistoryCheckpoint.builder().view(poolId)
          .epochCheckpoint(smallerCurrentEpoch - 1)
          .build();

      checkpoint.setEarnedReward(checkEarnedReward ? Boolean.TRUE : Boolean.FALSE);

      jdbcPoolHistoryCheckpointRepository.saveAll(
          List.of(checkpoint));
    }

    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  /**
   * fetch data using koios java client
   *
   * @param poolId
   * @return
   * @throws ApiException
   */
  private List<rest.koios.client.backend.api.pool.model.PoolHistory> getPoolHistoryList(
      String poolId)
      throws ApiException {
    return koiosClient.poolService()
        .getPoolHistory(poolId, null)
        .getValue();
  }

  /**
   * fetch current epoch in Koios
   *
   * @return
   * @throws ApiException
   */
  private Integer getCurrentEpochInKoios() throws ApiException {
    var tip = koiosClient.networkService().getChainTip().getValue();

    return tip.getEpochNo();
  }

  /**
   * get poolId list that are not in the checkpoint table or in the checkpoint table but have an
   * epoch checkpoint value < (current epoch - 1)
   *
   * @param poolIds
   * @return
   */
  @Override
  @SneakyThrows
  public List<String> getPoolIdListNeedFetchData(List<String> poolIds) {
    Integer currentEpoch = epochRepository.findMaxEpoch();
    int smallerCurrentEpoch = Math.min(currentEpoch, getCurrentEpochInKoios());

    Map<String, PoolHistoryCheckpoint> poolHistoryCheckpointMap = poolHistoryCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity()));

    return poolIds.stream()
        .filter(poolId -> (
            (!poolHistoryCheckpointMap.containsKey(poolId))
                || poolHistoryCheckpointMap.get(poolId).getEpochCheckpoint()
                < smallerCurrentEpoch - 1
                || !poolHistoryCheckpointMap.get(poolId).getEarnedReward()
        ))
        .collect(Collectors.toList());
  }

}
