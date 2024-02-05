package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
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

import io.micrometer.common.util.StringUtils;
import rest.koios.client.backend.api.base.exception.ApiException;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolHistoryRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;
import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolHistoryFetchingServiceImpl implements PoolHistoryFetchingService {

  final KoiosClient koiosClient;
  final PoolHistoryCheckpointRepository poolHistoryCheckpointRepository;
  final JOOQPoolHistoryRepository jooqPoolHistoryRepository;
  final JOOQPoolHistoryCheckpointRepository jooqPoolHistoryCheckpointRepository;
  final PoolHashRepository poolHashRepository;
  final EpochService epochService;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  @SneakyThrows
  public CompletableFuture<Boolean> fetchData(String poolId) {
    var poolHash = poolHashRepository.findByView(poolId);
    if (poolHash.isEmpty()) {
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    int currentEpoch = epochService.getCurrentEpoch();

    var dataFromKoios = getPoolHistoryList(poolId);
    var poolHistoryCheckpoint = poolHistoryCheckpointRepository.findByView(poolId);

    Map<Integer, PoolHistory> poolHistoryList =
        dataFromKoios.stream()
            .filter(poolHistory -> poolHistory.getEpochNo() < currentEpoch)
            .map(
                poolHistory ->
                    PoolHistory.builder()
                        .epochNo(poolHistory.getEpochNo())
                        .activeStake(
                            StringUtils.isNotBlank(poolHistory.getActiveStake())
                                ? new BigInteger(poolHistory.getActiveStake())
                                : null)
                        .activeStakePct(poolHistory.getActiveStakePct())
                        .blockCnt(poolHistory.getBlockCnt())
                        .poolFees(
                            StringUtils.isNotBlank(poolHistory.getPoolFees())
                                ? new BigInteger(poolHistory.getPoolFees())
                                : null)
                        .delegatorCnt(poolHistory.getDelegatorCnt())
                        .delegatorRewards(
                            StringUtils.isNotBlank(poolHistory.getDelegRewards())
                                ? new BigInteger(poolHistory.getDelegRewards())
                                : null)
                        .epochRos(poolHistory.getEpochRos())
                        .fixedCost(
                            StringUtils.isNotBlank(poolHistory.getFixedCost())
                                ? new BigInteger(poolHistory.getFixedCost())
                                : null)
                        .margin(poolHistory.getMargin())
                        .saturationPct(poolHistory.getSaturationPct())
                        .poolId(poolHash.get().getId())
                        .build())
            .collect(Collectors.toMap(PoolHistory::getEpochNo, Function.identity()));

    var poolHistoryCheck = poolHistoryList.get(currentEpoch - 2);
    boolean isSpendableReward =
        poolHistoryCheck == null
            || poolHistoryCheck.getPoolFees().compareTo(BigInteger.ZERO) != 0
            || poolHistoryCheck.getDelegatorRewards().compareTo(BigInteger.ZERO) != 0
            || poolHistoryCheck.getEpochRos() != 0.0;

    if (poolHistoryCheckpoint.isPresent()) {
      jooqPoolHistoryRepository.saveAll(
          poolHistoryList.values().stream()
              .filter(
                  poolHistory ->
                      poolHistory.getEpochNo()
                          > poolHistoryCheckpoint.get().getEpochCheckpoint() - 2)
              .collect(Collectors.toList()));

      var checkpoint = poolHistoryCheckpoint.get();
      checkpoint.setEpochCheckpoint(currentEpoch - 1);
      checkpoint.setIsSpendableReward(
          isSpendableReward || poolHistoryCheck.getBlockCnt() == 0 ? Boolean.TRUE : Boolean.FALSE);
      jooqPoolHistoryCheckpointRepository.saveAll(List.of(checkpoint));
    } else {
      jooqPoolHistoryRepository.saveAll(poolHistoryList.values().stream().toList());
      var checkpoint =
          PoolHistoryCheckpoint.builder().view(poolId).epochCheckpoint(currentEpoch - 1).build();

      checkpoint.setIsSpendableReward(isSpendableReward ? Boolean.TRUE : Boolean.FALSE);

      jooqPoolHistoryCheckpointRepository.saveAll(List.of(checkpoint));
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
      String poolId) throws ApiException {
    return koiosClient.poolService().getPoolHistory(poolId, null).getValue();
  }

  /**
   * get poolId list that are not in the checkpoint table or in the checkpoint table but have an
   * epoch checkpoint value < (current epoch - 1)
   *
   * @param poolIds
   * @return
   */
  @Override
  public List<String> getPoolIdListNeedFetchData(List<String> poolIds) throws ApiException {
    int currentEpoch = epochService.getCurrentEpoch();

    Map<String, PoolHistoryCheckpoint> poolHistoryCheckpointMap =
        poolHistoryCheckpointRepository.findByViewIn(poolIds).stream()
            .collect(Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity()));

    return poolIds.stream()
        .filter(
            poolId ->
                ((!poolHistoryCheckpointMap.containsKey(poolId))
                    || poolHistoryCheckpointMap.get(poolId).getEpochCheckpoint() < currentEpoch - 1
                    || !poolHistoryCheckpointMap.get(poolId).getIsSpendableReward()))
        .collect(Collectors.toList());
  }
}
