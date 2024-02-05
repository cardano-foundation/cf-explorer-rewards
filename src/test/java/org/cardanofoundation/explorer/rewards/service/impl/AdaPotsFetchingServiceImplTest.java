package org.cardanofoundation.explorer.rewards.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.network.model.Totals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.consumercommon.entity.AdaPots;
import org.cardanofoundation.explorer.consumercommon.entity.Block;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.AdaPotsRepository;
import org.cardanofoundation.explorer.rewards.repository.BlockRepository;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;

@ExtendWith(MockitoExtension.class)
class AdaPotsFetchingServiceImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock private AdaPotsRepository adaPotsRepository;

  @Mock private BlockRepository blockRepository;

  @Mock private EpochRepository epochRepository;

  @InjectMocks private AdaPotsFetchingServiceImpl adaPotsFetchingServiceImpl;

  @Captor ArgumentCaptor<AdaPots> adaPotsCaptor;

  @Test
  void testFetchData() throws Exception {
    // Setup
    Integer epoch = 315;

    var totals = new Totals();
    totals.setEpochNo(epoch);
    totals.setReserves("577641621267691");
    totals.setTreasury("806985387511233");
    totals.setReward("577641621267691");
    totals.setCirculation("32696853337370414");
    totals.setSupply("34085668605164388");

    when(koiosClient.networkService().getHistoricalTokenomicStatsByEpoch(epoch).getValue())
        .thenReturn(totals);
    Block block = Block.builder().id(1L).slotNo(1375L).build();
    when(blockRepository.getFirstBlockByEpochNo(epoch)).thenReturn(block);

    // Run the test
    final CompletableFuture<Boolean> result = adaPotsFetchingServiceImpl.fetchData(epoch);

    // Verify the results
    verify(adaPotsRepository).save(adaPotsCaptor.capture());

    assertEquals(adaPotsCaptor.getValue().getEpochNo(), epoch);
    assertEquals(adaPotsCaptor.getValue().getTreasury(), new BigInteger(totals.getTreasury()));
    assertEquals(adaPotsCaptor.getValue().getReserves(), new BigInteger(totals.getReserves()));
    assertEquals(adaPotsCaptor.getValue().getRewards(), new BigInteger(totals.getReward()));
    assertEquals(adaPotsCaptor.getValue().getUtxo(), new BigInteger(totals.getCirculation()));
    assertEquals(adaPotsCaptor.getValue().getBlockId(), block.getId());
    assertEquals(adaPotsCaptor.getValue().getSlotNo(), block.getSlotNo());

    assertTrue(result.get());
  }

  @Test
  void testGetEpochsNeedFetchData() {
    // Setup
    List<Integer> epochs = List.of(314, 315);
    when(epochRepository.findMaxEpoch()).thenReturn(316);

    final var adaPots = AdaPots.builder().id(1L).slotNo(1000L).epochNo(315).build();
    when(adaPotsRepository.findByEpochNoIn(epochs)).thenReturn(List.of(adaPots));

    // Run the test
    final List<Integer> result = adaPotsFetchingServiceImpl.getEpochsNeedFetchData(epochs);

    // Verify the results
    assertEquals(List.of(314), result);
  }
}
