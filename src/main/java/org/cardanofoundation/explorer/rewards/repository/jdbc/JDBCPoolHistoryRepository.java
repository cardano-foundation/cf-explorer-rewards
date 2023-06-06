package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.sql.Types;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;

import static org.cardanofoundation.explorer.rewards.util.CommonUtils.setNullableValue;

@Repository
@RequiredArgsConstructor
@Profile("koios")
public class JDBCPoolHistoryRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveAll(List<PoolHistory> poolHistoryList) {
    String sql =
        "INSERT INTO pool_history (id, pool_id, epoch_no, active_stake, active_stake_pct, saturation_pct, "
            + "block_cnt, delegator_cnt, margin, fixed_cost, pool_fees, deleg_rewards, epoch_ros)"
            + " VALUES (nextval('pool_history_id_seq'),?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + " ON CONFLICT (pool_id, epoch_no)"
            + " DO UPDATE SET"
            + " pool_fees = EXCLUDED.pool_fees,"
            + " deleg_rewards = EXCLUDED.deleg_rewards,"
            + " epoch_ros = EXCLUDED.epoch_ros";

    jdbcTemplate.batchUpdate(sql, poolHistoryList, batchSize, (ps, poolHistory) -> {
      ps.setString(1, poolHistory.getPoolId());
      ps.setInt(2, poolHistory.getEpochNo());

      setNullableValue(ps, 3, poolHistory.getActiveStake(), Types.VARCHAR);
      setNullableValue(ps, 4, poolHistory.getActiveStakePct(), Types.DOUBLE);
      setNullableValue(ps, 5, poolHistory.getSaturationPct(), Types.DOUBLE);
      setNullableValue(ps, 6, poolHistory.getBlockCnt(), Types.INTEGER);
      setNullableValue(ps, 7, poolHistory.getDelegatorCnt(), Types.INTEGER);
      setNullableValue(ps, 8, poolHistory.getMargin(), Types.DOUBLE);
      setNullableValue(ps, 9, poolHistory.getFixedCost(), Types.VARCHAR);
      setNullableValue(ps, 10, poolHistory.getPoolFees(), Types.VARCHAR);
      setNullableValue(ps, 11, poolHistory.getDelegRewards(), Types.VARCHAR);
      setNullableValue(ps, 12, poolHistory.getEpochRos(), Types.DOUBLE);
    });
  }

}
