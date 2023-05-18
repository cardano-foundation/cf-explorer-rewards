package org.cardanofoundation.explorer.rewards.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;

@Repository
public interface PoolHashRepository extends JpaRepository<PoolHash, Long> {
  List<PoolHash> findByViewIn(Collection<String> views);
}
