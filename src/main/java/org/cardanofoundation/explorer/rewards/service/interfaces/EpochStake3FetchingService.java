package org.cardanofoundation.explorer.rewards.service.interfaces;

import java.util.List;

import org.cardanofoundation.explorer.rewards.entity.EpochStake3;

public interface EpochStake3FetchingService {
  List<EpochStake3> fetchBatch(Integer start);
  List<EpochStake3> fetchData(List<String> stakeAddresses);
}
