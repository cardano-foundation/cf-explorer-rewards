package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.common.entity.ledgersync.RewardCheckpoint;

@Repository
public interface RewardCheckpointRepository extends JpaRepository<RewardCheckpoint, Long> {

  List<RewardCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
