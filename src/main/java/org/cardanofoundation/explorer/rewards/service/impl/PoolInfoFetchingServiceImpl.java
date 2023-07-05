package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.List;
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
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;
import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;
import org.cardanofoundation.explorer.rewards.service.PoolInfoFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolInfoFetchingServiceImpl implements PoolInfoFetchingService {

  final KoiosClient koiosClient;
  final JOOQPoolInfoRepository jooqPoolInfoRepository;
  final JOOQPoolInfoCheckpointRepository jooqPoolInfoCheckpointRepository;
  final PoolHashRepository poolHashRepository;
  final EpochService epochService;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  @SneakyThrows
  public CompletableFuture<Boolean> fetchData(List<String> poolIds) {
    var curTime = System.currentTimeMillis();

    var dataFromKoios = getPoolInfoList(poolIds);

    int currentEpoch = epochService.getCurrentEpoch();

    var poolHashMap = poolHashRepository.findByViewIn(poolIds).stream().collect(Collectors.toMap(
        PoolHash::getView, Function.identity()));

    if (poolHashMap.size() != poolIds.size()) {
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    List<PoolInfo> poolInfoList = dataFromKoios.stream().map(poolInfo ->
            PoolInfo.builder().poolId(poolHashMap.get(poolInfo.getPoolIdBech32()).getId())
                .activeStake(StringUtils.isNotBlank(poolInfo.getActiveStake()) ? new BigInteger(
                    poolInfo.getActiveStake()) : null)
                .fetchedAtEpoch(currentEpoch)
                .liveStake(StringUtils.isNotBlank(poolInfo.getLiveStake()) ? new BigInteger(
                    poolInfo.getLiveStake()) : null)
                .liveSaturation(poolInfo.getLiveSaturation()).build())
        .collect(Collectors.toList());

    log.info("fetch {} pool_info by koios api: {} ms, with poolIds input size {}",
        poolInfoList.size(), System.currentTimeMillis() - curTime, poolIds.size());

    List<PoolInfoCheckpoint> poolInfoCheckpointList = poolIds.stream()
        .map(poolId -> PoolInfoCheckpoint.builder()
            .view(poolId).epochCheckpoint(currentEpoch).build())
        .collect(Collectors.toList());

    jooqPoolInfoCheckpointRepository.saveAll(poolInfoCheckpointList);
    jooqPoolInfoRepository.saveAll(poolInfoList);

    return CompletableFuture.completedFuture(Boolean.TRUE);
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

}
