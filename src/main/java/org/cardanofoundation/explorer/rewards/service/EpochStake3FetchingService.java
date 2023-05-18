package org.cardanofoundation.explorer.rewards.service;

import java.util.List;

import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.entity.EpochStake3;

public interface EpochStake3FetchingService {
  List<EpochStake3> fetchData(List<String> stakeAddresses);
}
