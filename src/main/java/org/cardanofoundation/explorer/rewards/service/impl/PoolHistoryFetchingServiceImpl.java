package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.PoolHistory;
import org.cardanofoundation.explorer.rewards.entity.PoolHistoryCheckpoint;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHistoryRepository;
import org.cardanofoundation.explorer.rewards.repository.custom.CustomPoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.custom.CustomPoolHistoryRepository;
import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PoolHistoryFetchingServiceImpl implements PoolHistoryFetchingService {

  final KoiosClient koiosClient;
  final EpochRepository epochRepository;
  final PoolHistoryCheckpointRepository poolHistoryCheckpointRepository;
  final PoolHistoryRepository poolHistoryRepository;
  final CustomPoolHistoryRepository customPoolHistoryRepository;
  final CustomPoolHistoryCheckpointRepository customPoolHistoryCheckpointRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(
      String poolId) throws ApiException {
    var currentEpoch = epochRepository.findMaxEpoch();

    var dataFromKoios = getPoolHistoryList(poolId);
    var poolHistoryCheckpoint = poolHistoryCheckpointRepository.findByView(poolId);
    List<PoolHistory> poolHistoryList = new ArrayList<>();
    for (var poolHistory : dataFromKoios) {
      var entity = PoolHistory.builder()
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
          .build();
      poolHistoryList.add(entity);
    }

    if (poolHistoryCheckpoint.isPresent()) {
      customPoolHistoryRepository.savePoolHistoryList(poolHistoryList.stream().filter(
          poolHistory -> poolHistory.getEpochNo() > poolHistoryCheckpoint.get()
              .getEpochCheckpoint()).collect(Collectors.toList()));

      var checkpoint = poolHistoryCheckpoint.get();
      checkpoint.setEpochCheckpoint(currentEpoch - 1);
      customPoolHistoryCheckpointRepository.saveCheckpoints(List.of(checkpoint));
    } else {
      customPoolHistoryRepository.savePoolHistoryList(poolHistoryList);
      customPoolHistoryCheckpointRepository.saveCheckpoints(
          List.of(PoolHistoryCheckpoint.builder().view(poolId).epochCheckpoint(currentEpoch - 1)
              .build()));
    }

    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  private List<rest.koios.client.backend.api.pool.model.PoolHistory> getPoolHistoryList(
      String poolId)
      throws ApiException {
    return koiosClient.poolService()
        .getPoolHistory(poolId, null)
        .getValue();
  }

  private Map<String, PoolHistoryCheckpoint> getPoolHistoryCheckpointMap(
      List<String> poolIds) {

    Map<String, PoolHistoryCheckpoint> poolHistoryCheckpointMap = poolHistoryCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity()));

    List<PoolHistoryCheckpoint> poolHistoryCheckpoints = poolIds
        .stream()
        .filter(
            poolId -> !poolHistoryCheckpointMap.containsKey(poolId))
        .map(poolId -> PoolHistoryCheckpoint.builder()
            .view(poolId)
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());

    poolHistoryCheckpointMap.putAll(poolHistoryCheckpoints.stream().collect(
        Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity())));

    return poolHistoryCheckpointMap;
  }

  @Override
  public List<String> getPoolIdListNeedFetchData(List<String> poolIds) {
    Integer currentEpoch = epochRepository.findMaxEpoch();

    Map<String, PoolHistoryCheckpoint> poolHistoryCheckpointMap = poolHistoryCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolHistoryCheckpoint::getView, Function.identity()));

    return poolIds.stream()
        .filter(stakeAddress -> (
            (!poolHistoryCheckpointMap.containsKey(stakeAddress))
                || poolHistoryCheckpointMap.get(stakeAddress).getEpochCheckpoint()
                < currentEpoch - 1
        ))
        .collect(Collectors.toList());
  }

}
