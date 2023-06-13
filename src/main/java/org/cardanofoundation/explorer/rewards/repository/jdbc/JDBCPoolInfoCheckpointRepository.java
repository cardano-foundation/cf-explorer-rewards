package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;

@Repository
@Profile("koios")
public class JDBCPoolInfoCheckpointRepository {

  private final JdbcTemplate jdbcTemplate;

  private final int batchSize;

  public JDBCPoolInfoCheckpointRepository(JdbcTemplate jdbcTemplate,
                                          @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
                                          int batchSize) {
    this.jdbcTemplate = jdbcTemplate;
    this.batchSize = batchSize;
  }

  @Transactional
  public void saveAll(List<PoolInfoCheckpoint> poolInfoCheckpointList) {
    String sql = "INSERT INTO pool_info_checkpoint (id, view, epoch_checkpoint) "
        + " VALUES (nextval('pool_info_checkpoint_id_seq'), ?, ?)"
        + " ON CONFLICT (view) DO UPDATE SET epoch_checkpoint = EXCLUDED.epoch_checkpoint";

    jdbcTemplate.batchUpdate(sql, poolInfoCheckpointList, batchSize,
        (ps, poolInfoCheckpoint) -> {
          ps.setString(1, poolInfoCheckpoint.getView());
          ps.setLong(2, poolInfoCheckpoint.getEpochCheckpoint());
        });
  }
}
