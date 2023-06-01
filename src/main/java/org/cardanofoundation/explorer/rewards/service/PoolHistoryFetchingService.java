package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.base.exception.ApiException;

public interface PoolHistoryFetchingService {

  CompletableFuture<Boolean> fetchData(String poolIds) throws ApiException;

  List<String> getPoolIdListNeedFetchData(List<String> poolIds);
}
