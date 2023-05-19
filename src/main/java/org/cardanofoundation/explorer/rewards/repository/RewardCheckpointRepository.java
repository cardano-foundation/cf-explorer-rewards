package org.cardanofoundation.explorer.rewards.repository;

import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardCheckpointRepository extends JpaRepository<RewardCheckpoint, Long> {

  List<RewardCheckpoint> findByStakeAddressIn(List<String> stakeAddress);

}
