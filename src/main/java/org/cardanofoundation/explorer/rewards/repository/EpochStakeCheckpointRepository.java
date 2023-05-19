package org.cardanofoundation.explorer.rewards.repository;

import org.cardanofoundation.explorer.rewards.entity.EpochStakeCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpochStakeCheckpointRepository extends JpaRepository<EpochStakeCheckpoint, Long> {
  List<EpochStakeCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
