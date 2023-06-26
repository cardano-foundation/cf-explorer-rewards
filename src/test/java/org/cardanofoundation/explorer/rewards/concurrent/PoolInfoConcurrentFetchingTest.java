package org.cardanofoundation.explorer.rewards.concurrent;

import org.cardanofoundation.explorer.rewards.service.PoolInfoFetchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolInfoConcurrentFetchingTest {

  @Mock
  private PoolInfoFetchingService poolInfoFetchingService;

  @InjectMocks
  private PoolInfoConcurrentFetching poolInfoConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoStakeAddresses_ReturnsTrue() {
    List<String> poolIds = Collections.emptyList();
    boolean result = poolInfoConcurrentFetching.fetchDataConcurrently(poolIds);
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessfully_ReturnsTrue() throws Exception {
    // Setup
    List<String> poolIds = List.of(
        "pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt",
        "pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");

    poolInfoConcurrentFetching.setSubListSize(5);

    when(poolInfoFetchingService.fetchData(anyList()))
        .thenReturn(CompletableFuture.completedFuture(true));

    // Run the test
    final Boolean result = poolInfoConcurrentFetching.fetchDataConcurrently(
        poolIds);

    // Verify the results
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataFailed_ReturnsFalse() throws Exception {
    // Setup
    List<String> poolIds = List.of(
        "pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt",
        "pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");

    poolInfoConcurrentFetching.setSubListSize(5);

    when(poolInfoFetchingService.fetchData(anyList())).thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
    // Run the test
    final Boolean result = poolInfoConcurrentFetching.fetchDataConcurrently(
        poolIds);
    // Verify the results
    assertFalse(result);
  }
}
