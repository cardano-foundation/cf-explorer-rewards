package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
import org.cardanofoundation.explorer.rewards.repository.EpochStake3Repository;
import org.cardanofoundation.explorer.rewards.service.interfaces.EpochStake3StoringService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EpochStake3StoringServiceImpl implements EpochStake3StoringService {

  final EpochStake3Repository epochStakeRepository;

  @Override
  public void saveBatch(List<EpochStake3> epochStakes) {
    epochStakes.forEach(epochStake3 -> {
      var oldEpochStake = epochStakeRepository
          .findByEpochNoAndAddrAndPool(epochStake3.getEpochNo(), epochStake3.getAddr(),
              epochStake3.getPool());
      oldEpochStake.ifPresent(stake3 -> epochStake3.setId(stake3.getId()));
    });

    epochStakeRepository.saveAll(epochStakes);
  }

}
