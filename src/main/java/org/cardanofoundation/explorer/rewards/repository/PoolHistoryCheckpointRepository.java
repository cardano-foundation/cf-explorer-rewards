package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;
import java.util.Optional;

import javax.swing.text.html.Option;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.rewards.entity.PoolHistoryCheckpoint;

@Repository
public interface PoolHistoryCheckpointRepository extends JpaRepository<PoolHistoryCheckpoint, Long> {
  List<PoolHistoryCheckpoint> findByViewIn(List<String> poolId);
  Optional<PoolHistoryCheckpoint> findByView(String poolId);
}
