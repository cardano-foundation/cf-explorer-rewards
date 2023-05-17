package org.cardanofoundation.explorer.rewards.service.interfaces;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.AdaPots3;

public interface AdaPots3StoringService {
  @Transactional
  void saveBatch(List<AdaPots3> adaPotsList);
}
