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
  final JDBCPoolInfoRepository jdbcPoolInfoRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(List<String> poolIds) throws ApiException {
    var curTime = System.currentTimeMillis();

    var dataFromKoios = getPoolInfoList(poolIds);

    Integer currentEpoch = epochRepository.findMaxEpoch();
    int smallerCurrentEpoch = Math.min(currentEpoch, getCurrentEpochInKoios());

    List<PoolInfo> poolInfoList = dataFromKoios.stream().map(poolInfo ->
            PoolInfo.builder().poolId(poolInfo.getPoolIdBech32())
                .activeStake(poolInfo.getActiveStake())
                .fetchedAtEpoch(smallerCurrentEpoch)
                .liveStake(poolInfo.getLiveStake())
                .liveSaturation(poolInfo.getLiveSaturation()).build())
        .collect(Collectors.toList());

    log.info("fetch {} pool_info by koios api: {} ms, with poolIds input size {}",
        poolInfoList.size(), System.currentTimeMillis() - curTime, poolIds.size());

    jdbcPoolInfoRepository.saveAll(poolInfoList);

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

}
