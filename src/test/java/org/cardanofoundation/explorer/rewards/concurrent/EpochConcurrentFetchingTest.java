package org.cardanofoundation.explorer.rewards.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.common.entity.ledgersync.Epoch;
import org.cardanofoundation.explorer.rewards.service.EpochFetchingService;

@ExtendWith(MockitoExtension.class)
class EpochConcurrentFetchingTest {
  @Mock private EpochFetchingService epochFetchingService;
  @InjectMocks private EpochConcurrentFetching epochConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoEpochs_ReturnsTrue() {
    // Setup
    List<Integer> epochs = Collections.emptyList();

    // Run the test
    final List<Epoch> result = epochConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertTrue(result.isEmpty());
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessFully_ReturnsTrue() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(epochFetchingService.getEpochsNeedFetchData(epochs)).thenReturn(epochs);
    when(epochFetchingService.fetchData(314))
        .thenReturn(CompletableFuture.completedFuture(Epoch.builder().no(314).build()));
    when(epochFetchingService.fetchData(315))
        .thenReturn(CompletableFuture.completedFuture(Epoch.builder().no(315).build()));
    // Run the test
    final List<Epoch> result = epochConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertEquals(2, result.size());
  }

  @Test
  void fetchDataConcurrently_EpochsNeedFetchDataIsEmpty_ReturnsTrue() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(epochFetchingService.getEpochsNeedFetchData(anyList()))
        .thenReturn(Collections.emptyList());

    // Run the test
    final List<Epoch> result = epochConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertEquals(0, result.size());
  }
}
