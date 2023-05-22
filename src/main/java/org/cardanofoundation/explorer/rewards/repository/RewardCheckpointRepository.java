package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint;

public interface RewardCheckpointRepository extends JpaRepository<RewardCheckpoint, Long> {

  List<RewardCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
