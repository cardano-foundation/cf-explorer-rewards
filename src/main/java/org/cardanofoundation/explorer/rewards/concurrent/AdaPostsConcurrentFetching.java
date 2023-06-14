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

import org.cardanofoundation.explorer.rewards.service.AdaPotsFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class AdaPostsConcurrentFetching {

  final AdaPotsFetchingService adaPotsFetchingService;

  public Boolean fetchDataConcurrently(List<Integer> epochs) throws ApiException {
    //TODO: validate stake address list
    var curTime = System.currentTimeMillis();

    List<Integer> epochsNeedFetchData = adaPotsFetchingService.getEpochsNeedFetchData(epochs);

    if (epochsNeedFetchData.isEmpty()) {
      log.info("AdaPosts: all ada-posts had existed in AdaPosts table");
      return Boolean.TRUE;
    }

    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (var epoch : epochsNeedFetchData) {
      CompletableFuture<Boolean> future = adaPotsFetchingService.fetchData(epoch)
          .exceptionally(
              ex -> {
                log.error("Exception occurred in fetch ada-posts data}: {}", ex.getMessage());
                return Boolean.FALSE;
              }
          );
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save ada-posts record concurrently by koios api: {} ms",
             System.currentTimeMillis() - curTime);

    return result;
  }
}
