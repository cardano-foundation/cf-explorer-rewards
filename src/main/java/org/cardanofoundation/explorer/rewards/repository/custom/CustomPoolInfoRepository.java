package org.cardanofoundation.explorer.rewards.repository.custom;

import java.sql.Types;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.PoolInfo;

import static org.cardanofoundation.explorer.rewards.util.CommonUtils.setNullableValue;

@Repository
@RequiredArgsConstructor
public class CustomPoolInfoRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void savePoolInfoList(List<PoolInfo> poolInfoList) {
    String sql =
        "INSERT INTO pool_info (id, pool_id, fetched_at_epoch, active_stake, live_stake, live_saturation)"
            + " VALUES (nextval('pool_info_id_seq'),?, ?, ?, ?, ?) "
            + " ON CONFLICT (pool_id, fetched_at_epoch) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, poolInfoList, batchSize, (ps, poolInfo) -> {
      ps.setString(1, poolInfo.getPoolId());
      ps.setInt(2, poolInfo.getFetchedAtEpoch());

      setNullableValue(ps, 3, poolInfo.getActiveStake(), Types.VARCHAR);
      setNullableValue(ps, 4, poolInfo.getLiveStake(), Types.VARCHAR);
      setNullableValue(ps, 5, poolInfo.getLiveSaturation(), Types.DOUBLE);
    });
  }
}
