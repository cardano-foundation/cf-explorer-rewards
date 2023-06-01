package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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

    var dataFromKoios = getPoolHistoryList(poolId);
    var poolHistoryCheckpoint = poolHistoryCheckpointRepository.findByView(poolId);

    List<PoolHistory> poolHistoryList =
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
            .build()).collect(Collectors.toList());

    if (poolHistoryCheckpoint.isPresent()) {
      jdbcPoolHistoryRepository.saveAll(poolHistoryList.stream().filter(
          poolHistory -> poolHistory.getEpochNo() > poolHistoryCheckpoint.get()
              .getEpochCheckpoint()).collect(Collectors.toList()));

      var checkpoint = poolHistoryCheckpoint.get();
      checkpoint.setEpochCheckpoint(currentEpoch - 1);
      jdbcPoolHistoryCheckpointRepository.saveAll(List.of(checkpoint));
    } else {
      jdbcPoolHistoryRepository.saveAll(poolHistoryList);
      jdbcPoolHistoryCheckpointRepository.saveAll(
          List.of(PoolHistoryCheckpoint.builder().view(poolId).epochCheckpoint(currentEpoch - 1)
              .build()));
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
   * get poolId list that are not in the checkpoint table or in the checkpoint table but have an
   * epoch checkpoint value < (current epoch - 1)
   *
   * @param poolIds
   * @return
   */
  @Override
  public List<String> getPoolIdListNeedFetchData(List<String> poolIds) {
    Integer currentEpoch = epochRepository.findMaxEpoch();

    Map<String, PoolHistoryCheckpoint> poolHistoryCheckpointMap = poolHistoryCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity()));

    return poolIds.stream()
        .filter(poolId -> (
            (!poolHistoryCheckpointMap.containsKey(poolId))
                || poolHistoryCheckpointMap.get(poolId).getEpochCheckpoint()
                < currentEpoch - 1
        ))
        .collect(Collectors.toList());
  }

}
