package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.EpochStakeFetchingService;
import org.cardanofoundation.explorer.rewards.service.PoolInfoFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PoolInfoConcurrentFetching {
  final PoolInfoFetchingService poolInfoFetchingService;

  @Value("${application.epoch-stake.list-size-each-thread}")
  int subListSize;

  public Boolean fetchDataConcurrently(List<String> poolIds) throws ApiException {
    //TODO: validate poolIds list
    var curTime = System.currentTimeMillis();
    // we only fetch data with addresses that are not in the checkpoint table
    // or in the checkpoint table but have an epoch checkpoint value < current epoch
    List<String> poolIdListNeedFetchData = poolInfoFetchingService.getPoolIdListNeedFetchData(poolIds);

    if (poolIdListNeedFetchData.isEmpty()) {
      log.info(
          "PoolInfo: all pool id were in checkpoint and had epoch checkpoint = current epoch");
      return Boolean.TRUE;
    }

    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < poolIdListNeedFetchData.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, poolIdListNeedFetchData.size());
      var sublist = poolIdListNeedFetchData.subList(i, endIndex);

      CompletableFuture<Boolean> future = poolInfoFetchingService.fetchData(sublist)
          .exceptionally(
              ex -> {
                log.error("Exception occurred in fetch epoch stake data}: {}", ex.getMessage());
                return Boolean.FALSE;
              }
          );
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save pool info record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
