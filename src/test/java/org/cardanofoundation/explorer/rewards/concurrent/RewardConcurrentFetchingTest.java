package org.cardanofoundation.explorer.rewards.concurrent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.base.exception.ApiException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.rewards.service.RewardFetchingService;

@ExtendWith(MockitoExtension.class)
class RewardConcurrentFetchingTest {

  @Mock private RewardFetchingService rewardFetchingService;

  @InjectMocks private RewardConcurrentFetching rewardConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoStakeAddresses_ReturnsTrue() {
    List<String> stakeAddressList = Collections.emptyList();
    boolean result = rewardConcurrentFetching.fetchDataConcurrently(stakeAddressList);
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessfully_ReturnsTrue() throws Exception {
    // Setup
    List<String> stakeAddressList =
        List.of(
            "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
            "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    rewardConcurrentFetching.setSubListSize(5);

    when(rewardFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenReturn(stakeAddressList);
    when(rewardFetchingService.fetchData(anyList()))
        .thenReturn(CompletableFuture.completedFuture(true));

    // Run the test
    final Boolean result = rewardConcurrentFetching.fetchDataConcurrently(stakeAddressList);

    // Verify the results
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_ApiExceptionOccurs_ReturnsFalse() throws Exception {
    List<String> stakeAddressList =
        List.of(
            "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
            "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    when(rewardFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenThrow(ApiException.class);

    boolean result = rewardConcurrentFetching.fetchDataConcurrently(stakeAddressList);

    assertFalse(result);
  }

  @Test
  void fetchDataConcurrently_StakeAddressListNeedFetchDataIsEmpty_ReturnsTrue() throws Exception {
    List<String> stakeAddressList =
        List.of(
            "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
            "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    when(rewardFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenReturn(Collections.emptyList());

    boolean result = rewardConcurrentFetching.fetchDataConcurrently(stakeAddressList);

    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataFailed_ReturnsFalse() throws Exception {
    // Setup
    List<String> stakeAddressList =
        List.of(
            "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
            "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");
    rewardConcurrentFetching.setSubListSize(5);
    when(rewardFetchingService.getStakeAddressListNeedFetchData(anyList()))
        .thenReturn(stakeAddressList);

    when(rewardFetchingService.fetchData(anyList()))
        .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
    // Run the test
    final Boolean result = rewardConcurrentFetching.fetchDataConcurrently(stakeAddressList);
    // Verify the results
    assertFalse(result);
  }
}
