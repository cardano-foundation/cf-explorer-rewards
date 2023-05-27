package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Reward4FetchingService {
  CompletableFuture<Boolean> fetchData(List<String> stakeAddresses);
}
