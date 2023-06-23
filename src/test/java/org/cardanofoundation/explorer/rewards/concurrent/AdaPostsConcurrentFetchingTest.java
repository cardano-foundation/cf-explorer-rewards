package org.cardanofoundation.explorer.rewards.concurrent;

import org.cardanofoundation.explorer.rewards.service.AdaPotsFetchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaPostsConcurrentFetchingTest {
  @Mock
  private AdaPotsFetchingService adaPotsFetchingService;
  @InjectMocks
  private AdaPostsConcurrentFetching adaPostsConcurrentFetching;

  @Test
  void fetchDataConcurrently_NoEpochs_ReturnsTrue() {
    // Setup
    List<Integer> epochs = Collections.emptyList();

    // Run the test
    final Boolean result = adaPostsConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataSuccessFully_ReturnsTrue() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(adaPotsFetchingService.getEpochsNeedFetchData(epochs)).thenReturn(epochs);
    when(adaPotsFetchingService.fetchData(anyInt()))
        .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    // Run the test
    final Boolean result = adaPostsConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_EpochsNeedFetchDataIsEmpty_ReturnsTrue() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(adaPotsFetchingService.getEpochsNeedFetchData(anyList()))
        .thenReturn(Collections.emptyList());

    // Run the test
    final Boolean result = adaPostsConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertTrue(result);
  }

  @Test
  void fetchDataConcurrently_FetchDataFailed_ReturnsFalse() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(adaPotsFetchingService.getEpochsNeedFetchData(epochs)).thenReturn(epochs);
    when(adaPotsFetchingService.fetchData(anyInt()))
        .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));

    // Run the test
    final Boolean result = adaPostsConcurrentFetching.fetchDataConcurrently(epochs);

    // Verify the results
    assertThat(result).isFalse();
  }
}
