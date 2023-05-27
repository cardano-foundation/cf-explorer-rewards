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

import org.cardanofoundation.explorer.rewards.service.Reward4FetchingService;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class Reward4ConcurrentFetching {
  final Reward4FetchingService reward4FetchingService;

  @Value("${application.reward.list-size-each-thread}")
  int subListSize;

  public Boolean fetchDataConcurrently(List<String> stakeAddressList) {
    var curTime = System.currentTimeMillis();

    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < stakeAddressList.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, stakeAddressList.size());
      var sublist = stakeAddressList.subList(i, endIndex);

      CompletableFuture<Boolean> future = reward4FetchingService.fetchData(sublist);
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save reward record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
