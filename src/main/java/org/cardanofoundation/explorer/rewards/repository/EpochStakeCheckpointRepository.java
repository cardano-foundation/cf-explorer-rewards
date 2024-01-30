package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;

@Repository
public interface EpochStakeCheckpointRepository extends JpaRepository<EpochStakeCheckpoint, Long> {
  List<EpochStakeCheckpoint> findByStakeAddressIn(List<String> stakeAddress);
}
