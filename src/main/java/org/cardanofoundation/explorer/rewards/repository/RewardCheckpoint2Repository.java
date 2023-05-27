package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint2;

@Repository
public interface RewardCheckpoint2Repository extends JpaRepository<RewardCheckpoint2, Long> {
  List<RewardCheckpoint2> findByStakeAddressIn(List<String> stakeAddress);
}
