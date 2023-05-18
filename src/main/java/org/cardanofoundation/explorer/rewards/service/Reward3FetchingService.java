package org.cardanofoundation.explorer.rewards.service;

import java.util.List;


public interface Reward3FetchingService {
  Boolean fetchData(List<String> stakeAddresses);
}
