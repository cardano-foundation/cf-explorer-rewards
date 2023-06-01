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

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;
import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolInfoCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jdbc.JDBCPoolInfoCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jdbc.JDBCPoolInfoRepository;
import org.cardanofoundation.explorer.rewards.service.PoolInfoFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolInfoFetchingServiceImpl implements PoolInfoFetchingService {

  final KoiosClient koiosClient;
  final EpochRepository epochRepository;
  final PoolInfoCheckpointRepository poolInfoCheckpointRepository;
  final JDBCPoolInfoRepository jdbcPoolInfoRepository;
  final JDBCPoolInfoCheckpointRepository jdbcPoolInfoCheckpointRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(List<String> poolIds) throws ApiException {
    var curTime = System.currentTimeMillis();

    var dataFromKoios = getPoolInfoList(poolIds);

    Integer currentEpoch = epochRepository.findMaxEpoch();

    List<PoolInfo> poolInfoList = dataFromKoios.stream().map(poolInfo ->
            PoolInfo.builder().poolId(poolInfo.getPoolIdBech32())
                .activeStake(poolInfo.getActiveStake())
                .fetchedAtEpoch(currentEpoch)
                .liveStake(poolInfo.getLiveStake())
                .liveSaturation(poolInfo.getLiveSaturation()).build())
        .collect(Collectors.toList());

    log.info("fetch {} pool_info by koios api: {} ms, with poolIds input size {}",
        poolInfoList.size(), System.currentTimeMillis() - curTime, poolIds.size());

    var poolInfoCheckpointMap = getPoolInfoCheckpointMap(poolIds);

    List<PoolInfo> saveData = poolInfoList.stream()
        .filter(poolInfo -> poolInfoCheckpointMap.containsKey(poolInfo.getPoolId())
            && poolInfo.getFetchedAtEpoch() > poolInfoCheckpointMap.get(poolInfo.getPoolId())
            .getEpochCheckpoint()).collect(Collectors.toList());

    poolInfoCheckpointMap.values()
        .forEach(poolInfoCheckpoint -> poolInfoCheckpoint.setEpochCheckpoint(currentEpoch));

    jdbcPoolInfoCheckpointRepository.saveAll(poolInfoCheckpointMap.values().stream().toList());
    jdbcPoolInfoRepository.saveAll(saveData);

    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  private Map<String, PoolInfoCheckpoint> getPoolInfoCheckpointMap(List<String> poolIds) {
    Map<String, PoolInfoCheckpoint> poolInfoCheckpointMap = poolInfoCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolInfoCheckpoint::getView, Function.identity()));

    List<PoolInfoCheckpoint> epochStakeCheckpoints = poolIds
        .stream()
        .filter(
            poolId -> !poolInfoCheckpointMap.containsKey(poolId))
        .map(poolId -> PoolInfoCheckpoint.builder()
            .view(poolId)
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());

    // put all into result
    poolInfoCheckpointMap.putAll(epochStakeCheckpoints.stream().collect(
        Collectors.toMap(PoolInfoCheckpoint::getView, Function.identity())));

    return poolInfoCheckpointMap;
  }

  /**
   * fetch data using koios java client
   *
   * @param poolIdList
   * @return
   * @throws ApiException
   */
  private List<rest.koios.client.backend.api.pool.model.PoolInfo> getPoolInfoList(
      List<String> poolIdList)
      throws ApiException {
    return koiosClient.poolService()
        .getPoolInformation(poolIdList, null)
        .getValue();
  }

  @Override
  public List<String> getPoolIdListNeedFetchData(List<String> poolIds) {
    Integer currentEpoch = epochRepository.findMaxEpoch();

    Map<String, PoolInfoCheckpoint> poolInfoCheckpointMap = poolInfoCheckpointRepository
        .findByViewIn(poolIds)
        .stream()
        .collect(Collectors.toMap(PoolInfoCheckpoint::getView, Function.identity()));

    return poolIds.stream()
        .filter(poolId -> (
            (!poolInfoCheckpointMap.containsKey(poolId))
                || poolInfoCheckpointMap.get(poolId).getEpochCheckpoint()
                < currentEpoch
        ))
        .collect(Collectors.toList());
  }
}
