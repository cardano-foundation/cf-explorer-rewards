package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.Epoch;

@Repository
public interface EpochRepository extends JpaRepository<Epoch, Long> {
  @Query("SELECT MAX(e.no) FROM Epoch e")
  Integer findMaxEpoch();
}
