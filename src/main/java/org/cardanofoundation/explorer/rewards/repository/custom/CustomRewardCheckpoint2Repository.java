package org.cardanofoundation.explorer.rewards.repository.custom;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint2;

@Repository
@RequiredArgsConstructor
public class CustomRewardCheckpoint2Repository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveCheckpoints(List<RewardCheckpoint2> rewardCheckpoints) {
    String sql = "INSERT INTO reward_checkpoint_2 (id, view, epoch_checkpoint) "
        + " VALUES (nextval('reward_checkpoint_2_id_seq'), ?, ?)"
        + "    ON CONFLICT (view) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, rewardCheckpoints, batchSize, (ps, rewardCheckpoint) -> {
      ps.setString(1, rewardCheckpoint.getStakeAddress());
      ps.setLong(2, rewardCheckpoint.getEpochCheckpoint());
    });
  }

}
