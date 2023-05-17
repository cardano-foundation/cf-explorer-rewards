package org.cardanofoundation.explorer.rewards.service.interfaces;

import java.util.List;

import org.cardanofoundation.explorer.rewards.entity.Reward3;

public interface Reward3FetchingService {
  List<Reward3> fetchBatch(Integer start);
  List<Reward3> fetchData(List<String> stakeAddresses);
}
