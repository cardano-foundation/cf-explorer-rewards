package org.cardanofoundation.explorer.rewards.schedule;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.schedule.service.PoolInfoDataService;

@ExtendWith(MockitoExtension.class)
class PoolInfoDataScheduleTest {

  @Mock private PoolHashRepository poolHashRepository;
  @Mock private PoolInfoDataService poolInfoDataService;
  @InjectMocks private PoolInfoDataSchedule poolInfoDataSchedule;

  @Test
  void fetchAllPoolInfoData_WhenFetchDataSuccessfully() {
    // Setup
    final List<PoolHash> poolHashes =
        List.of(
            PoolHash.builder()
                .id(1L)
                .view("pool1chdvqec5lwsxuedrdhcdcxg2295tqrce9lcz3luru5fruzjf3wr")
                .build(),
            PoolHash.builder()
                .id(2L)
                .view("pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc")
                .build());
    poolInfoDataSchedule.setSubListSize(1);
    when(poolHashRepository.findAll()).thenReturn(poolHashes);
    when(poolInfoDataService.fetchData(anyList()))
        .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

    // Run the test
    poolInfoDataSchedule.fetchAllPoolInfoData();

    // Verify
    verify(poolInfoDataService, times(2)).fetchData(anyList());
  }

  @Test
  void fetchAllPoolInfoData_whenPoolHashRepositoryReturnsNoItem() {
    // Setup
    when(poolHashRepository.findAll()).thenReturn(List.of());
    // Run the test
    poolInfoDataSchedule.fetchAllPoolInfoData();
    // Verify
    verify(poolInfoDataService, never()).fetchData(anyList());
  }

  @Test
  void fetchAllPoolInfoData_whenPoolInfoDataServiceReturnsFailure() {
    // Setup
    final List<PoolHash> poolHashes =
        List.of(
            PoolHash.builder()
                .id(1L)
                .view("pool1chdvqec5lwsxuedrdhcdcxg2295tqrce9lcz3luru5fruzjf3wr")
                .build(),
            PoolHash.builder()
                .id(2L)
                .view("pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc")
                .build());

    when(poolHashRepository.findAll()).thenReturn(poolHashes);
    poolInfoDataSchedule.setSubListSize(1);
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeExceptionally(new Exception());
    when(poolInfoDataService.fetchData(anyList())).thenReturn(future);

    // Run the test
    poolInfoDataSchedule.fetchAllPoolInfoData();

    // Verify
    verify(poolInfoDataService, times(2)).fetchData(anyList());
  }
}
