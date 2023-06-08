package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.PoolInfoFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class PoolInfoConcurrentFetching {

  final PoolInfoFetchingService poolInfoFetchingService;

  @Value("${application.pool-info.list-size-each-thread}")
  int subListSize;

  public Boolean fetchDataConcurrently(List<String> poolIds) throws ApiException {
    //TODO: validate poolIds list
    var curTime = System.currentTimeMillis();
    if (poolIds.isEmpty()) {
      return Boolean.TRUE;
    }
    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < poolIds.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, poolIds.size());
      var sublist = poolIds.subList(i, endIndex);

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
