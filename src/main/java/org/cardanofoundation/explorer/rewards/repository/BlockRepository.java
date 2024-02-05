package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.explorer.consumercommon.entity.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {

  @Query(
      "SELECT blockOuter FROM Block blockOuter "
          + "WHERE blockOuter.id = "
          + "(SELECT MIN(block.id) FROM Block block WHERE block.epochNo = :epochNo)")
  Block getFirstBlockByEpochNo(@Param("epochNo") int epochNo);
}
