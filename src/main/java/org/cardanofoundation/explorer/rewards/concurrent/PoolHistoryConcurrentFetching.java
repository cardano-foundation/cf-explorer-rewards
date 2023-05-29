package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PoolHistoryConcurrentFetching {

  final PoolHistoryFetchingService poolHistoryFetchingService;

  public Boolean fetchDataConcurrently(List<String> poolIds) throws ApiException {
    //todo: (important) validate poolIds
    var curTime = System.currentTimeMillis();

    List<String> poolIdListNeedFetchData = poolHistoryFetchingService.getPoolIdListNeedFetchData(
        poolIds);

    if (poolIdListNeedFetchData.isEmpty()) {
      log.info(
          "Reward: all poolId were in checkpoint and had epoch checkpoint = current epoch - 1");
      return Boolean.TRUE;
    }
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
