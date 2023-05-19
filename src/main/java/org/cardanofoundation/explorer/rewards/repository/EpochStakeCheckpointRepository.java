package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.cardanofoundation.explorer.rewards.entity.EpochStakeCheckpoint;

public interface EpochStakeCheckpointRepository extends JpaRepository<EpochStakeCheckpoint, Long> {
  List<EpochStakeCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
