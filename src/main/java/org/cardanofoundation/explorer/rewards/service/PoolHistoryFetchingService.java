package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.cardanofoundation.explorer.rewards.entity.PoolHistory;
import rest.koios.client.backend.api.base.exception.ApiException;

public interface PoolHistoryFetchingService {

  CompletableFuture<Boolean> fetchData(String poolIds);

  List<String> getPoolIdListNeedFetchData(List<String> poolIds);
}
