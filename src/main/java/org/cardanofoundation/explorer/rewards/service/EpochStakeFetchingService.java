package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EpochStakeFetchingService {
  CompletableFuture<Boolean> fetchData(List<String> stakeAddressList);

  List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList);
}
