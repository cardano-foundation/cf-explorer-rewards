package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.rewards.entity.EpochStake3;

@Repository
public interface EpochStake3Repository extends JpaRepository<EpochStake3, Long> {

}
