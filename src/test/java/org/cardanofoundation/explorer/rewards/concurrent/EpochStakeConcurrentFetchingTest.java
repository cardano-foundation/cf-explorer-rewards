package org.cardanofoundation.explorer.rewards.concurrent;

import org.cardanofoundation.explorer.rewards.service.EpochStakeFetchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.base.exception.ApiException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EpochStakeConcurrentFetchingTest {

  @Mock
  private EpochStakeFetchingService epochStakeFetchingService;

  @InjectMocks
  private EpochStakeConcurrentFetching epochStakeConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoStakeAddresses_ReturnsTrue() {
    List<String> stakeAddressList = Collections.emptyList();
    boolean result = epochStakeConcurrentFetching.fetchDataConcurrently(stakeAddressList);
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_ApiExceptionOccurs_ReturnsFalse()
      throws Exception {
    List<String> stakeAddressList = List.of(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    when(epochStakeFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenThrow(ApiException.class);

    boolean result = epochStakeConcurrentFetching.fetchDataConcurrently(stakeAddressList);

    assertFalse(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessfully_ReturnsTrue() throws Exception {
    // Setup
    List<String> stakeAddressList = List.of(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    epochStakeConcurrentFetching.setSubListSize(5);

    when(epochStakeFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenReturn(stakeAddressList);

    when(epochStakeFetchingService.fetchData(anyList()))
        .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    // Run the test
    final Boolean result = epochStakeConcurrentFetching.fetchDataConcurrently(
        stakeAddressList);

    // Verify the results
    assertThat(result).isTrue();
  }

  @Test
  void fetchDataConcurrently_StakeAddressListNeedFetchDataIsEmpty_ReturnsTrue()
      throws Exception {
    List<String> stakeAddressList = List.of(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    when(epochStakeFetchingService.getStakeAddressListNeedFetchData(stakeAddressList))
        .thenReturn(Collections.emptyList());

    boolean result = epochStakeConcurrentFetching.fetchDataConcurrently(stakeAddressList);

    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataFailed_ReturnsFalse() throws Exception {
    // Setup
    List<String> stakeAddressList = List.of(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");
    epochStakeConcurrentFetching.setSubListSize(5);
    when(epochStakeFetchingService.getStakeAddressListNeedFetchData(anyList()))
        .thenReturn(stakeAddressList);

    when(epochStakeFetchingService.fetchData(anyList())).thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
    // Run the test
    final Boolean result = epochStakeConcurrentFetching.fetchDataConcurrently(
        stakeAddressList);
    // Verify the results
    assertFalse(result);
  }
}
