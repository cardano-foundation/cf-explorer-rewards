package org.cardanofoundation.explorer.rewards.schedule.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.base.exception.ApiException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHash;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfo;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQPoolInfoRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;

@ExtendWith(MockitoExtension.class)
class PoolInfoDataServiceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock private JOOQPoolInfoRepository jooqPoolInfoRepository;
  @Mock private PoolHashRepository poolHashRepository;
  @Mock private EpochService epochService;
  @InjectMocks private PoolInfoDataService poolInfoDataService;
  @Captor ArgumentCaptor<List<PoolInfo>> poolInfoCaptor;

  @Test
  void fetchData_whenFetchSuccessfully() throws Exception {
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

    when(epochService.getCurrentEpoch()).thenReturn(416);

    when(koiosClient.poolService().getPoolInformation(poolIds, null).getValue())
        .thenReturn(List.of(poolInfo1, poolInfo2));

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
    final CompletableFuture<Boolean> result = poolInfoDataService.fetchData(poolIds);

    // Verify
    verify(jooqPoolInfoRepository).saveAll(poolInfoCaptor.capture());
    assertEquals(2, poolInfoCaptor.getValue().size());
    assertTrue(result.get());
  }

  @Test
  void fetchData_whenEpochServiceThrowsApiException() throws Exception {
    // Setup
    List<String> poolIds =
        List.of(
            "pool1chdvqec5lwsxuedrdhcdcxg2295tqrce9lcz3luru5fruzjf3wr",
            "pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc");

    when(epochService.getCurrentEpoch()).thenThrow(ApiException.class);

    // Run the test
    assertThatThrownBy(() -> poolInfoDataService.fetchData(poolIds))
        .isInstanceOf(ApiException.class);
  }
}
