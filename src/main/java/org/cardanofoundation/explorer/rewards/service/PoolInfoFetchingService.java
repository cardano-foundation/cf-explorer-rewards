package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PoolInfoFetchingService {
  CompletableFuture<Boolean> fetchData(List<String> poolIds);
}
