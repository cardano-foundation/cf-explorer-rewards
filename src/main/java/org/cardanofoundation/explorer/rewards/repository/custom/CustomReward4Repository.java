package org.cardanofoundation.explorer.rewards.repository.custom;


import java.sql.Types;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.Reward4;

@Repository
@RequiredArgsConstructor
public class CustomReward4Repository {
  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveRewards(List<Reward4> rewards) {
    String sql = "INSERT INTO reward4 (id, type, amount, earned_epoch, spendable_epoch, addr_id, "
        + "pool_id)"
        + " VALUES (nextval('reward4_id_seq'), ?, ?, ?, ?, ?, ?)"
        + " ON CONFLICT (addr_id, type, earned_epoch, pool_id) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, rewards, batchSize, (ps, reward) -> {
      ps.setString(1, reward.getType().getValue());
      ps.setLong(2, reward.getAmount().longValue());
      ps.setLong(3, reward.getEarnedEpoch());
      ps.setLong(4, reward.getSpendableEpoch());
      if (reward.getPool() != null) {
        ps.setLong(6, reward.getPool().getId());
      } else {
        ps.setNull(6, Types.BIGINT);
      }
    });
  }
}
