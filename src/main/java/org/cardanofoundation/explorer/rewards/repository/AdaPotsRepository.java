package org.cardanofoundation.explorer.rewards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import org.cardanofoundation.explorer.consumercommon.entity.AdaPots;

public interface AdaPotsRepository extends JpaRepository<AdaPots, Integer> {

  List<AdaPots> findByEpochNoIn(@Param("epochs") List<Integer> epochs);
}
