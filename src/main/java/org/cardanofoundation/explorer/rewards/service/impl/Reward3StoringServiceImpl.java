package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.repository.Reward3Repository;
import org.cardanofoundation.explorer.rewards.service.interfaces.Reward3StoringService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Reward3StoringServiceImpl implements Reward3StoringService {

  final Reward3Repository rewardRepository;

  @Override
  public void saveBatch(List<Reward3> rewards) {
    rewards.forEach(reward -> {
      var oldReward = rewardRepository.findByStakeAddressIdAndPoolIdAndEarnedEpochAndType(
          reward.getStakeAddressId(), reward.getPoolId(), reward.getEarnedEpoch(),
          reward.getType());

      oldReward.ifPresent(reward3 -> reward.setId(reward3.getId()));
    });

    rewardRepository.saveAll(rewards);
  }
}
