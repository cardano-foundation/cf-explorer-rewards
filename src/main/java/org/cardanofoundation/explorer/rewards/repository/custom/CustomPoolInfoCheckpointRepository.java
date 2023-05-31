package org.cardanofoundation.explorer.rewards.repository.custom;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;

@Repository
@RequiredArgsConstructor
public class CustomPoolInfoCheckpointRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveCheckpoints(List<PoolInfoCheckpoint> poolInfoCheckpointList) {
    String sql = "INSERT INTO pool_info_checkpoint (id, view, epoch_checkpoint) "
        + " VALUES (nextval('pool_info_checkpoint_id_seq'), ?, ?)"
        + "    ON CONFLICT (view) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, poolInfoCheckpointList, batchSize,
        (ps, poolInfoCheckpoint) -> {
          ps.setString(1, poolInfoCheckpoint.getView());
          ps.setLong(2, poolInfoCheckpoint.getEpochCheckpoint());
        });
  }
}
