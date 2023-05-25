package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.Reward3FetchingService;
import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class Reward3ConcurrentFetching {

  final Reward3FetchingService reward3FetchingService;

  @Value("${application.reward.list-size-each-thread}")
  int subListSize;

  public Boolean fetchDataConcurrently(List<String> stakeAddressList) {
    var curTime = System.currentTimeMillis();

    List<CompletableFuture<List<AccountRewards>>> futures = new ArrayList<>();

    for (int i = 0; i < stakeAddressList.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, stakeAddressList.size());
      var sublist = stakeAddressList.subList(i, endIndex);

      try {
        CompletableFuture<List<AccountRewards>> future = reward3FetchingService.fetchData(sublist);
        futures.add(future);
      } catch (ApiException e) {
        log.info("ApiException: {}", e.getMessage());
        return Boolean.FALSE;
      }
    }
    var allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    CompletableFuture<List<AccountRewards>> combinedFuture = allFutures.thenApply(v ->
        futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList())
    );

    try {
      List<AccountRewards> accountRewardsList = combinedFuture.get();
      reward3FetchingService.storeData(stakeAddressList, accountRewardsList);
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      return Boolean.FALSE;
    } catch (Exception e) {
      return Boolean.FALSE;
    }

    log.info("Fetch and save reward record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return Boolean.TRUE;

  }
}
