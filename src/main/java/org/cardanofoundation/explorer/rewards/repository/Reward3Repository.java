package org.cardanofoundation.explorer.rewards.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.entity.Reward3;

@Repository
public interface Reward3Repository extends JpaRepository<Reward3, Long> {
  Optional<Reward3> findByStakeAddressIdAndPoolIdAndEarnedEpochAndType(Long stakeAddressId, Long poolId,
                                                                       Integer earnedEpoch, RewardType type);
}
