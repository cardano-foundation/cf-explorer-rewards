package org.cardanofoundation.explorer.rewards.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.cardanofoundation.explorer.consumercommon.entity.Epoch;

public interface EpochFetchingService {

  CompletableFuture<Epoch> fetchData(Integer epochNo);

  List<Integer> getEpochsNeedFetchData(List<Integer> epochNoList);
}
