package org.cardanofoundation.explorer.rewards.repository.jdbc;


import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.Reward;

@Repository
@Profile("koios")
public class JDBCRewardRepository {

  private final JdbcTemplate jdbcTemplate;

  private int batchSize;

  public JDBCRewardRepository(JdbcTemplate jdbcTemplate,
                              @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize) {
    this.jdbcTemplate = jdbcTemplate;
    this.batchSize = batchSize;
  }

  @Transactional
  public void saveAll(List<Reward> rewards) {
    String sql = "INSERT INTO reward (id, type, amount, earned_epoch, spendable_epoch, addr_id, "
        + "pool_id)"
        + " VALUES (nextval('reward_id_seq'), ?, ?, ?, ?, ?, ?)"
        + " ON CONFLICT (addr_id, type, earned_epoch, pool_id) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, rewards, batchSize, (ps, reward) -> {
      ps.setString(1, reward.getType().getValue());
      ps.setLong(2, reward.getAmount().longValue());
      ps.setLong(3, reward.getEarnedEpoch());
      ps.setLong(4, reward.getSpendableEpoch());
      if (reward.getAddr() != null) {
        ps.setLong(5, reward.getAddr().getId());
      } else {
        ps.setNull(5, Types.BIGINT);
      }
      if (reward.getPool() != null) {
        ps.setLong(6, reward.getPool().getId());
      } else {
        ps.setNull(6, Types.BIGINT);
      }
    });
  }
}
