package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;

@Repository
@RequiredArgsConstructor
@Profile("koios")
public class JDBCPoolHistoryCheckpointRepository {

  private final JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private int batchSize;

  @Transactional
  public void saveAll(List<PoolHistoryCheckpoint> poolHistoryCheckpoints) {
    String sql = "INSERT INTO pool_history_checkpoint (id, view, epoch_checkpoint) "
        + " VALUES (nextval('pool_history_checkpoint_id_seq'), ?, ?)"
        + "    ON CONFLICT (view) DO NOTHING";

    jdbcTemplate.batchUpdate(sql, poolHistoryCheckpoints, batchSize,
        (ps, poolHistoryCheckpoint) -> {
          ps.setString(1, poolHistoryCheckpoint.getView());
          ps.setLong(2, poolHistoryCheckpoint.getEpochCheckpoint());
        });
  }
}
