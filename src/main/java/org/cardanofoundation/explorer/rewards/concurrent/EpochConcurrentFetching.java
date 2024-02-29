package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.common.entity.ledgersync.Epoch;
import org.cardanofoundation.explorer.rewards.service.EpochFetchingService;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class EpochConcurrentFetching {

  final EpochFetchingService epochFetchingService;

  public List<Epoch> fetchDataConcurrently(List<Integer> epochNoList) {
    var curTime = System.currentTimeMillis();

    List<Integer> epochsNeedFetchData = epochFetchingService.getEpochsNeedFetchData(epochNoList);

    if (epochsNeedFetchData.isEmpty()) {
      log.info("Epochs: all epoch had reward distributed");
      return new ArrayList<>();
    }

    // fetch and store data concurrently
    List<CompletableFuture<Epoch>> futures = new ArrayList<>();

    for (var epoch : epochsNeedFetchData) {
      CompletableFuture<Epoch> future =
          epochFetchingService
              .fetchData(epoch)
              .exceptionally(
                  ex -> {
                    log.error("Exception occurred in fetch epoch data}: {}", ex.getMessage());
                    return null;
                  });
      if (Objects.nonNull(future)) {
        futures.add(future);
      }
    }

    List<Epoch> result =
        futures.stream()
            .filter(epoch -> Objects.nonNull(epoch.join()))
            .map(CompletableFuture::join)
            .toList();

    log.info(
        "Fetch and save epoch record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
