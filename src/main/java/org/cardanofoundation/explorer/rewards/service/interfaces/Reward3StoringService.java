package org.cardanofoundation.explorer.rewards.service.interfaces;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.Reward3;

public interface Reward3StoringService {
  @Transactional
  void saveBatch(List<Reward3> rewards);
}
