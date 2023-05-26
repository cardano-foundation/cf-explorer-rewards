package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.base.exception.ApiException;

public interface EpochStakeFetchingService {
  CompletableFuture<List<AccountHistory>> fetchData(List<String> stakeAddressList) throws ApiException;

  void storeData(List<String> stakeAddressList, List<AccountHistory> accountHistoryList);
}
