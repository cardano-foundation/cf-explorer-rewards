package org.cardanofoundation.explorer.rewards.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class EpochServiceImpl implements EpochService {

  private final EpochRepository epochRepository;
  private final KoiosClient koiosClient;

  private Integer getCurrentEpochInKoios() throws ApiException {
    var tip = koiosClient.networkService().getChainTip().getValue();

    return tip.getEpochNo();
  }

  @Override
  public int getCurrentEpoch() throws ApiException {
    Integer maxEpoch = epochRepository.findMaxEpoch();
    int currentEpoch;

    if (maxEpoch != null) {
      currentEpoch = Math.min(epochRepository.findMaxEpoch(), getCurrentEpochInKoios());
    } else {
      currentEpoch = 0;
    }

    return currentEpoch;
  }
}
