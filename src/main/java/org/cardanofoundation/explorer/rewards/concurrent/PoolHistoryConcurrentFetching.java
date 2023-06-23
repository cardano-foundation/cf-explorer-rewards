package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolHistoryConcurrentFetching {

  final PoolHistoryFetchingService poolHistoryFetchingService;

  public Boolean fetchDataConcurrently(List<String> poolIds) {
    //TODO: validate poolIds
    var curTime = System.currentTimeMillis();

    if (poolIds.isEmpty()) {
      return Boolean.TRUE;
    }

    List<String> poolIdListNeedFetchData;
    try {
      // we only fetch data with addresses that are not in the checkpoint table
      // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
      poolIdListNeedFetchData = poolHistoryFetchingService.getPoolIdListNeedFetchData(
          poolIds);
    } catch (ApiException e) {
      log.error("Exception occurs when calling getPoolIdListNeedFetchData: {}", e.getMessage());
      return Boolean.FALSE;
    }

    if (poolIdListNeedFetchData.isEmpty()) {
      log.info(
          "Reward: all poolId were in checkpoint and had epoch checkpoint = current epoch - 1");
      return Boolean.TRUE;
    }

    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (var poolId : poolIdListNeedFetchData) {
      CompletableFuture<Boolean> future = poolHistoryFetchingService.fetchData(poolId)
          .exceptionally(
              ex -> {
                log.error("Exception occurred in fetchData for poolId {}: {}", poolId,
                    ex.getMessage());
                return Boolean.FALSE;
              }
          );
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save pool history record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
