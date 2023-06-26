package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.rewards.service.RewardFetchingService;
import rest.koios.client.backend.api.base.exception.ApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class RewardConcurrentFetching {

  final RewardFetchingService rewardFetchingService;

  @Setter
  @Value("${application.reward.list-size-each-thread}")
  int subListSize;

  public Boolean fetchDataConcurrently(List<String> stakeAddressList) {
    //TODO: validate stake address list
    var curTime = System.currentTimeMillis();

    if (stakeAddressList.isEmpty()) {
      return Boolean.TRUE;
    }

    List<String> stakeAddressListNeedFetchData;
    try {
      // we only fetch data with addresses that are not in the checkpoint table
      // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
      stakeAddressListNeedFetchData = rewardFetchingService.getStakeAddressListNeedFetchData(
          stakeAddressList);
    } catch (ApiException e) {
      log.error("Exception occurs when calling getStakeAddressListNeedFetchData: {}", e.getMessage());
      return Boolean.FALSE;
    }

    if (stakeAddressListNeedFetchData.isEmpty()) {
      log.info(
          "Reward: all stake addresses were in checkpoint and had epoch checkpoint = current epoch - 1");
      return Boolean.TRUE;
    }
    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < stakeAddressList.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, stakeAddressList.size());
      var sublist = stakeAddressList.subList(i, endIndex);

      CompletableFuture<Boolean> future = rewardFetchingService.fetchData(sublist).exceptionally(
          ex -> {
            log.error("Exception occurred in fetch reward data: {}", ex.getMessage());
            return Boolean.FALSE;
          }
      );
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save reward record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
