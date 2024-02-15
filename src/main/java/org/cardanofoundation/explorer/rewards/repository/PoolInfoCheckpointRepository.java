package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint;

@Repository
public interface PoolInfoCheckpointRepository extends JpaRepository<PoolInfoCheckpoint, Long> {

  List<PoolInfoCheckpoint> findByViewIn(List<String> poolId);
}
