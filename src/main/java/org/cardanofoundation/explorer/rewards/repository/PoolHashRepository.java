package org.cardanofoundation.explorer.rewards.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;

@Repository
public interface PoolHashRepository extends JpaRepository<PoolHash, Long> {

  Optional<PoolHash> findById(Long id);

  List<PoolHash> findByHashRawIn(Collection<String> hashes);

  Optional<PoolHash> findByView(String view);
}
