package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.rewards.entity.Reward3;

@Repository
public interface RewardRepository extends JpaRepository<Reward3, Long> {
}
