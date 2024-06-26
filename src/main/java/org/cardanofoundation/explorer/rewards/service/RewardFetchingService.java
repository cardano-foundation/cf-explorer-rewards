package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.base.exception.ApiException;

public interface RewardFetchingService {
  CompletableFuture<Boolean> fetchData(List<String> stakeAddresses);

  List<String> getStakeAddressListNeedFetchData(List<String> StakeAddressList) throws ApiException;
}
