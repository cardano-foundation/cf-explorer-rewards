package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.rewards.entity.Reward4;

@Repository
public interface Reward4Repository extends JpaRepository<Reward4, Long> {
}
