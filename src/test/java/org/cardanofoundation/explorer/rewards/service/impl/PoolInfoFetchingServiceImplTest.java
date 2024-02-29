package org.cardanofoundation.explorer.rewards.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHash;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfo;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;

@ExtendWith(MockitoExtension.class)
class PoolInfoFetchingServiceImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock private EpochService epochService;
  @Mock private PoolHashRepository poolHashRepository;
  @Mock private JOOQPoolInfoRepository jooqPoolInfoRepository;
  @Mock private JOOQPoolInfoCheckpointRepository jooqPoolInfoCheckpointRepository;
  @InjectMocks private PoolInfoFetchingServiceImpl poolInfoFetchingServiceImpl;

  @Captor ArgumentCaptor<List<PoolInfo>> poolInfoCaptor;

  @Captor ArgumentCaptor<List<PoolInfoCheckpoint>> checkpointCaptor;

  @Test
  void fetchData_shouldFetchAndSavePoolInfoData() throws Exception {
    // Setup
    List<String> poolIds =
        List.of(
            "pool1chdvqec5lwsxuedrdhcdcxg2295tqrce9lcz3luru5fruzjf3wr",
            "pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc");

    var poolInfo1 = new rest.koios.client.backend.api.pool.model.PoolInfo();
    poolInfo1.setPoolIdBech32(poolIds.get(0));
    poolInfo1.setActiveStake("6481489374");
    poolInfo1.setLiveStake("6481489374");
    poolInfo1.setLiveSaturation(0.01);

    var poolInfo2 = new rest.koios.client.backend.api.pool.model.PoolInfo();
    poolInfo2.setPoolIdBech32(poolIds.get(1));
    poolInfo2.setActiveStake("44968129248");
    poolInfo2.setLiveStake("5722279429");
    poolInfo2.setLiveSaturation(0.01);

    when(koiosClient.poolService().getPoolInformation(poolIds, null).getValue())
        .thenReturn(List.of(poolInfo1, poolInfo2));
    when(epochService.getCurrentEpoch()).thenReturn(416);
    when(poolHashRepository.findByViewIn(poolIds))
        .thenReturn(
            List.of(
                PoolHash.builder()
                    .id(1L)
                    .view("pool1chdvqec5lwsxuedrdhcdcxg2295tqrce9lcz3luru5fruzjf3wr")
                    .build(),
                PoolHash.builder()
                    .id(2L)
                    .view("pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc")
                    .build()));
    // Run the test
    final CompletableFuture<Boolean> result = poolInfoFetchingServiceImpl.fetchData(poolIds);

    // verify
    verify(jooqPoolInfoCheckpointRepository).saveAll(checkpointCaptor.capture());
    verify(jooqPoolInfoRepository).saveAll(poolInfoCaptor.capture());
    assertEquals(2, poolInfoCaptor.getValue().size());
    assertEquals(2, checkpointCaptor.getValue().size());
    assertTrue(result.get());
  }
}
