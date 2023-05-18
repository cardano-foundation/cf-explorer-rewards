package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint;

public interface RewardCheckpointRepository extends JpaRepository<RewardCheckpoint, Long> {

  Optional<RewardCheckpoint> findByStakeAddress(String stakeAddress);

  List<RewardCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
