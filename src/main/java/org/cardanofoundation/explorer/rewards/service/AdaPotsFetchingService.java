package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.base.exception.ApiException;

public interface AdaPotsFetchingService {
  CompletableFuture<Boolean> fetchData(Integer epoch) throws ApiException;

  List<Integer> getEpochsNeedFetchData(List<Integer> epochs);
}
