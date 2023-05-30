package org.cardanofoundation.explorer.rewards.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.Reward;


@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
}
