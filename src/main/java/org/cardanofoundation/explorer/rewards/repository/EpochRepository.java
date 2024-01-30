package org.cardanofoundation.explorer.rewards.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.Epoch;

@Repository
public interface EpochRepository extends JpaRepository<Epoch, Long> {
  @Query("SELECT MAX(e.no) FROM Epoch e")
  Integer findMaxEpoch();

  Optional<Epoch> findByNo(Integer no);

  List<Epoch> findByRewardsDistributedIsNotNull();

  @Query(
      "UPDATE Epoch epoch SET epoch.rewardsDistributed = :rewardsDistributed WHERE epoch.no = :no")
  @Modifying
  void updateRewardDistributedByNo(
      @Param("rewardsDistributed") BigInteger rewardsDistributed, @Param("no") Integer no);
}
