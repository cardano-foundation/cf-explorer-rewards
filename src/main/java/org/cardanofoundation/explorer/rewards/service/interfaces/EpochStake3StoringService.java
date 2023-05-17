package org.cardanofoundation.explorer.rewards.service.interfaces;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.EpochStake3;

public interface EpochStake3StoringService {

  @Transactional
  void saveBatch(List<EpochStake3> epochStakes);

}
