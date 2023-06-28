package org.cardanofoundation.explorer.rewards.schedule.service;

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

import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.common.util.StringUtils;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoRepository;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PoolInfoDataService {

  final KoiosClient koiosClient;
  final JOOQPoolInfoRepository jooqPoolInfoRepository;
  final PoolHashRepository poolHashRepository;
  final EpochRepository epochRepository;

  @Transactional
  @Retryable(retryFor = {Exception.class}, maxAttempts = 3)
  @SneakyThrows
  @Async
  public CompletableFuture<Boolean> fetchData(List<String> poolIds) {
    var curTime = System.currentTimeMillis();
    int currentEpoch = Math.min(epochRepository.findMaxEpoch(), getCurrentEpochInKoios());
    var dataFromKoios = getPoolInfoList(poolIds);

    var poolHashMap = poolHashRepository.findByViewIn(poolIds).stream().collect(Collectors.toMap(
        PoolHash::getView, Function.identity()));

    List<PoolInfo> poolInfoList = dataFromKoios.stream().map(poolInfo ->
            PoolInfo.builder().pool(poolHashMap.get(poolInfo.getPoolIdBech32()))
                .activeStake(StringUtils.isNotBlank(poolInfo.getActiveStake()) ? new BigInteger(
                    poolInfo.getActiveStake()) : null)
                .fetchedAtEpoch(currentEpoch)
                .liveStake(StringUtils.isNotBlank(poolInfo.getLiveStake()) ? new BigInteger(
                    poolInfo.getLiveStake()) : null)
                .liveSaturation(poolInfo.getLiveSaturation()).build())
        .collect(Collectors.toList());

    log.info("fetch {} pool_info by koios api: {} ms, with poolIds input size {}",
        poolInfoList.size(), System.currentTimeMillis() - curTime, poolIds.size());

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
