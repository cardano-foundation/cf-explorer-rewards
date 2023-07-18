package org.cardanofoundation.explorer.rewards.service;

import org.cardanofoundation.explorer.consumercommon.entity.Epoch;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EpochFetchingService {

  CompletableFuture<Epoch> fetchData(Integer epochNo);

  List<Integer> getEpochsNeedFetchData(List<Integer> epochNoList);
}
