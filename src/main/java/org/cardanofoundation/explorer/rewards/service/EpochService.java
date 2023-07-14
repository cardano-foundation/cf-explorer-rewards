package org.cardanofoundation.explorer.rewards.service;


import rest.koios.client.backend.api.base.exception.ApiException;

public interface EpochService {

  int getCurrentEpoch() throws ApiException;
}
