package org.cardanofoundation.explorer.rewards.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.epoch.model.EpochInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.consumercommon.entity.Epoch;
import org.cardanofoundation.explorer.consumercommon.enumeration.EraType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;

@ExtendWith(MockitoExtension.class)
class EpochFetchingServiceImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock private EpochRepository epochRepository;

  @InjectMocks private EpochFetchingServiceImpl epochFetchingService;

  @Test
  void testFetchDataWithByronBlock() {
    // Setup
    Integer epoch = 315;

    var epochInfo = new EpochInfo();
    epochInfo.setEpochNo(epoch);
    epochInfo.setTotalRewards("577641621267691");

    when(epochRepository.findByNo(any()))
        .thenReturn(Optional.ofNullable(Epoch.builder().no(epoch).era(EraType.BYRON).build()));

    // Run the test
    final CompletableFuture<Epoch> result = epochFetchingService.fetchData(epoch);
    assertNull(result);
  }

  @Test
  void testFetchData() throws Exception {
    // Setup
    Integer epoch = 315;

    var epochInfo = new EpochInfo();
    epochInfo.setEpochNo(epoch);
    epochInfo.setTotalRewards("577641621267691");

    when(epochRepository.findByNo(any()))
        .thenReturn(Optional.ofNullable(Epoch.builder().no(epoch).era(EraType.SHELLEY).build()));
    when(koiosClient.epochService().getEpochInformationByEpoch(epoch).getValue())
        .thenReturn(epochInfo);
    doNothing()
        .when(epochRepository)
        .updateRewardDistributedByNo(new BigInteger("577641621267691"), epoch);

    // Run the test
    final CompletableFuture<Epoch> result = epochFetchingService.fetchData(epoch);

    assertEquals(315, result.get().getNo());
    assertEquals(result.get().getRewardsDistributed(), new BigInteger("577641621267691"));
  }

  @Test
  void testFetchDataWithEpochNotNull() {
    // Setup
    Integer epoch = 315;

    var epochInfo = new EpochInfo();
    epochInfo.setEpochNo(epoch);
    epochInfo.setTotalRewards("577641621267691");

    when(epochRepository.findByNo(any()))
        .thenReturn(
            Optional.ofNullable(
                Epoch.builder()
                    .no(epoch)
                    .rewardsDistributed(new BigInteger("577641621267691"))
                    .build()));
    // Run the test
    final CompletableFuture<Epoch> result = epochFetchingService.fetchData(epoch);
    assertNull(result);
  }

  @Test
  void testFetchDataWithEpochNotFound() {
    // Setup
    Integer epoch = 5000;

    when(epochRepository.findByNo(any())).thenReturn(Optional.empty());
    // Run the test
    final CompletableFuture<Epoch> result = epochFetchingService.fetchData(epoch);

    assertNull(result);
  }

  @Test
  void testGetEpochsNeedFetchData() {
    // Setup
    List<Epoch> epochs = new ArrayList<>();
    epochs.add(Epoch.builder().no(312).build());
    epochs.add(Epoch.builder().no(313).build());
    when(epochRepository.findMaxEpoch()).thenReturn(316);

    when(epochRepository.findByRewardsDistributedIsNotNull()).thenReturn(epochs);

    // Run the test
    final List<Integer> result =
        epochFetchingService.getEpochsNeedFetchData(List.of(312, 313, 314, 315, 316));

    // Verify the results
    assertEquals(List.of(314), result);
  }
}
