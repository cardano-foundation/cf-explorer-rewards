package org.cardanofoundation.explorer.rewards.repository.custom;

import java.sql.Types;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;

import static org.cardanofoundation.explorer.rewards.util.CommonUtils.setNullableValue;

@Repository
@RequiredArgsConstructor
public class CustomEpochStakeRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveEpochStakes(List<EpochStake> epochStakeList) {
    String sql = "INSERT INTO epoch_stake3 (id, epoch_no, amount, addr_id, pool_id)"
        + " VALUES (nextval('epoch_stake3_id_seq'), ?, ?, ?, ?)"
        + " ON CONFLICT (addr_id, epoch_no, pool_id) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, epochStakeList, batchSize, (ps, epochStake) -> {
      setNullableValue(ps, 1, epochStake.getEpochNo(), Types.INTEGER);
      setNullableValue(ps, 2, epochStake.getAmount(), Types.BIGINT);

      if (epochStake.getAddr() != null) {
        ps.setLong(3, epochStake.getAddr().getId());
      } else {
        ps.setNull(3, Types.BIGINT);
      }

      if (epochStake.getPool() != null) {
        ps.setLong(4, epochStake.getPool().getId());
      } else {
        ps.setNull(4, Types.BIGINT);
      }
    });
  }

}
