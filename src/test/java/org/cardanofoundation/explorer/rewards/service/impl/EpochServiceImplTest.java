package org.cardanofoundation.explorer.rewards.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;

@ExtendWith(MockitoExtension.class)
class EpochServiceImplTest {

  @Mock private EpochRepository epochRepository;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @InjectMocks private EpochServiceImpl epochServiceImpl;

  @Test
  void testGetCurrentEpoch() throws Exception {
    // Setup
    when(epochRepository.findMaxEpoch()).thenReturn(415);
    when(koiosClient.networkService().getChainTip().getValue().getEpochNo()).thenReturn(414);

    // Run the test
    final int result = epochServiceImpl.getCurrentEpoch();

    // Verify the results
    assertEquals(414, result);
  }

  @Test
  void testGetCurrentEpoch_whenEpochRepositoryReturnsNull() throws Exception {
    // Setup
    when(epochRepository.findMaxEpoch()).thenReturn(null);

    // Run the test
    final int result = epochServiceImpl.getCurrentEpoch();

    // Verify the results
    assertEquals(0, result);
  }
}
