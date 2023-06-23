package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AdaPotsFetchingService {
  CompletableFuture<Boolean> fetchData(Integer epoch);

  List<Integer> getEpochsNeedFetchData(List<Integer> epochs);
}
