package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHistory;

@Repository
public interface PoolHistoryRepository extends JpaRepository<PoolHistory, Long> {}
