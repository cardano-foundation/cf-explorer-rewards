package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rest.koios.client.backend.api.base.exception.ApiException;

public interface PoolInfoFetchingService {

  CompletableFuture<Boolean> fetchData(List<String> poolIds) throws ApiException;
}
