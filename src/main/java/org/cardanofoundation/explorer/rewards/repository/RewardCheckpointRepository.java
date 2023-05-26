package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;

@Repository
public interface RewardCheckpointRepository extends JpaRepository<RewardCheckpoint, Long> {

  List<RewardCheckpoint> findByStakeAddressIn(List<String> stakeAddress);

}
