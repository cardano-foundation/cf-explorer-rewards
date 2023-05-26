package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.exception.ApiException;


public interface Reward3FetchingService {
  CompletableFuture<List<AccountRewards>> fetchData(List<String> stakeAddressList) throws ApiException;

  void storeData(List<String> stakeAddressList, List<AccountRewards> accountRewardsList);
}
