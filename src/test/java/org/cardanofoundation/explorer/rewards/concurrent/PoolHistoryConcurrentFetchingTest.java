package org.cardanofoundation.explorer.rewards.concurrent;

import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolHistoryConcurrentFetchingTest {

  @Mock
  private PoolHistoryFetchingService poolHistoryFetchingService;

  @InjectMocks
  private PoolHistoryConcurrentFetching poolHistoryConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoPoolIds_ReturnsTrue() {
    List<String> poolIds = Collections.emptyList();
    boolean result = poolHistoryConcurrentFetching.fetchDataConcurrently(poolIds);
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessfully_ReturnsTrue() throws Exception {
    // Setup
    List<String> poolIds = List.of(
        "pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt",
        "pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");
    when(poolHistoryFetchingService.getPoolIdListNeedFetchData(poolIds))
        .thenReturn(poolIds);
    when(poolHistoryFetchingService.fetchData(anyString()))
        .thenReturn(CompletableFuture.completedFuture(true));

    // Run the test
    final Boolean result = poolHistoryConcurrentFetching.fetchDataConcurrently(
        poolIds);

    // Verify the results
    assertThat(result).isTrue();
  }

  @Test
  void fetchDataConcurrently_PoolIdListNeedFetchDataIsEmpty_ReturnsTrue()
      throws Exception {
    // Setup
    List<String> poolIds = List.of(
            "pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt",
            "pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");
    when(poolHistoryFetchingService.getPoolIdListNeedFetchData(poolIds))
        .thenReturn(Collections.emptyList());

    // Run the test
    final Boolean result = poolHistoryConcurrentFetching.fetchDataConcurrently(poolIds);

    // Verify the results
    assertThat(result).isTrue();
  }

  @Test
  void fetchDataConcurrently_FetchDataFailed_ReturnsFalse() throws Exception {
    // Setup
    List<String> poolIds = List.of(
        "pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt",
        "pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");
    when(poolHistoryFetchingService.getPoolIdListNeedFetchData(poolIds))
        .thenReturn(poolIds);

    when(poolHistoryFetchingService.fetchData(anyString())).thenReturn(
        CompletableFuture.completedFuture(Boolean.FALSE));
    // Run the test
    final Boolean result = poolHistoryConcurrentFetching.fetchDataConcurrently(
        poolIds);
    // Verify the results
    assertFalse(result);
  }
}
