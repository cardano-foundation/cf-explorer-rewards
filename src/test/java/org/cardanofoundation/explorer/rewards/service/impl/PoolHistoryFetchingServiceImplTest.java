package org.cardanofoundation.explorer.rewards.service.impl;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolHistoryCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.rewards.service.EpochService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolHistoryFetchingServiceImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock
  private EpochService epochService;

  @Mock
  private PoolHistoryCheckpointRepository poolHistoryCheckpointRepository;

  @Mock
  private PoolHashRepository poolHashRepository;

  @Mock
  private JOOQPoolHistoryRepository jooqPoolHistoryRepository;

  @Mock
  private JOOQPoolHistoryCheckpointRepository jooqPoolHistoryCheckpointRepository;

  @InjectMocks
  private PoolHistoryFetchingServiceImpl poolHistoryFetchingServiceImpl;

  @Captor
  ArgumentCaptor<List<PoolHistory>> poolHistoryCaptor;

  @Captor
  ArgumentCaptor<List<PoolHistoryCheckpoint>> checkpointCaptor;

  @Test
  void fetchData_shouldFetchAndSavePoolHistoryData() throws Exception {
    // Setup
    String poolId = "pool10quq4wlghwrvmhdzc7geq22pyhzegccwj56ax2g8hx0cjfahufs";
    when(poolHashRepository.findByView(poolId)).thenReturn(
        Optional.of(PoolHash.builder().id(1L).view(poolId).build()));
    when(epochService.getCurrentEpoch()).thenReturn(416);

    var poolHistory1 = new rest.koios.client.backend.api.pool.model.PoolHistory();
    poolHistory1.setEpochNo(415);
    poolHistory1.setPoolFees("59063025857");
    poolHistory1.setActiveStake("64328594406327");
    poolHistory1.setDelegRewards("0");
    poolHistory1.setActiveStakePct(0.27302943272682884);
    poolHistory1.setEpochRos(0.0);
    poolHistory1.setFixedCost("500000000");
    poolHistory1.setMargin(1.0);
    poolHistory1.setSaturationPct(null);
    poolHistory1.setBlockCnt(72);
    poolHistory1.setDelegatorCnt(2);

    var poolHistory2 = new rest.koios.client.backend.api.pool.model.PoolHistory();
    poolHistory2.setEpochNo(414);
    poolHistory2.setPoolFees("59063025857");
    poolHistory2.setActiveStake("64328992063709");
    poolHistory2.setDelegRewards("0");
    poolHistory2.setActiveStakePct(0.29189705705190994);
    poolHistory2.setEpochRos(0.0);
    poolHistory2.setFixedCost("500000000");
    poolHistory2.setMargin(1.0);
    poolHistory2.setSaturationPct(89.74);
    poolHistory2.setBlockCnt(64);
    poolHistory2.setDelegatorCnt(2);

    when(koiosClient.poolService().getPoolHistory(poolId, null).getValue()).thenReturn(
        List.of(poolHistory1, poolHistory2));

    final Optional<PoolHistoryCheckpoint> poolHistoryCheckpoint = Optional.of(
        PoolHistoryCheckpoint.builder().epochCheckpoint(416).view(poolId).id(1L).build());

    when(poolHistoryCheckpointRepository.findByView(poolId))
        .thenReturn(poolHistoryCheckpoint);

    // Run the test
    final CompletableFuture<Boolean> result = poolHistoryFetchingServiceImpl.fetchData(
        poolId);

    // verify
    verify(jooqPoolHistoryRepository).saveAll(poolHistoryCaptor.capture());
    verify(jooqPoolHistoryCheckpointRepository).saveAll(checkpointCaptor.capture());
    assertEquals(1, poolHistoryCaptor.getValue().size());
    assertEquals(1, checkpointCaptor.getValue().size());

    assertTrue(result.get());
  }

  @Test
  void fetchData_WhenPoolHistoryCheckpointRepositoryReturnsAbsent() throws Exception {
    // Setup
    String poolId = "pool10quq4wlghwrvmhdzc7geq22pyhzegccwj56ax2g8hx0cjfahufs";
    when(poolHashRepository.findByView(poolId)).thenReturn(
        Optional.of(PoolHash.builder().id(1L).view(poolId).build()));
    when(epochService.getCurrentEpoch()).thenReturn(416);

    var poolHistory1 = new rest.koios.client.backend.api.pool.model.PoolHistory();
    poolHistory1.setEpochNo(415);
    poolHistory1.setPoolFees("59063025857");
    poolHistory1.setActiveStake("64328594406327");
    poolHistory1.setDelegRewards("0");
    poolHistory1.setActiveStakePct(0.27302943272682884);
    poolHistory1.setEpochRos(0.0);
    poolHistory1.setFixedCost("500000000");
    poolHistory1.setMargin(1.0);
    poolHistory1.setSaturationPct(null);
    poolHistory1.setBlockCnt(72);
    poolHistory1.setDelegatorCnt(2);

    var poolHistory2 = new rest.koios.client.backend.api.pool.model.PoolHistory();
    poolHistory2.setEpochNo(414);
    poolHistory2.setPoolFees("59063025857");
    poolHistory2.setActiveStake("64328992063709");
    poolHistory2.setDelegRewards("0");
    poolHistory2.setActiveStakePct(0.29189705705190994);
    poolHistory2.setEpochRos(0.0);
    poolHistory2.setFixedCost("500000000");
    poolHistory2.setMargin(1.0);
    poolHistory2.setSaturationPct(89.74);
    poolHistory2.setBlockCnt(64);
    poolHistory2.setDelegatorCnt(2);

    when(koiosClient.poolService().getPoolHistory(poolId, null).getValue()).thenReturn(
        List.of(poolHistory1, poolHistory2));

    when(poolHistoryCheckpointRepository.findByView(poolId))
        .thenReturn(Optional.empty());

    // Run the test
    final CompletableFuture<Boolean> result = poolHistoryFetchingServiceImpl.fetchData(poolId);

    // Verify
    verify(jooqPoolHistoryRepository).saveAll(poolHistoryCaptor.capture());
    verify(jooqPoolHistoryCheckpointRepository).saveAll(checkpointCaptor.capture());
    assertEquals(2, poolHistoryCaptor.getValue().size());
    assertEquals(1, checkpointCaptor.getValue().size());

    assertTrue(result.get());
  }

  @Test
  void getPoolIdListNeedFetchData_ShouldReturnPoolIds() throws Exception {
    // Setup
    String poolId = "pool10quq4wlghwrvmhdzc7geq22pyhzegccwj56ax2g8hx0cjfahufs";
    when(epochService.getCurrentEpoch()).thenReturn(416);

    final List<PoolHistoryCheckpoint> poolHistoryCheckpoints = List.of(
        PoolHistoryCheckpoint.builder().view(poolId).epochCheckpoint(415).isSpendableReward(true)
            .build());
    when(poolHistoryCheckpointRepository.findByViewIn(List.of(poolId)))
        .thenReturn(poolHistoryCheckpoints);

    // Run the test
    final List<String> result = poolHistoryFetchingServiceImpl.getPoolIdListNeedFetchData(
        List.of(poolId));

    // Verify the results
    assertThat(result).isEqualTo(List.of());
  }
}
