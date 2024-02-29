package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@Component
public class JOOQPoolInfoCheckpointRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQPoolInfoCheckpointRepository(
      DSLContext dsl, @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, PoolInfoCheckpoint.class);
  }

  public void saveAll(List<PoolInfoCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var epochCheckpointField = entityUtil.getColumnField(PoolInfoCheckpoint_.EPOCH_CHECKPOINT);
    var viewField = entityUtil.getColumnField(PoolInfoCheckpoint_.VIEW);

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(viewField), checkpoint.getView())
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint())
              .onConflict(field(viewField))
              .doUpdate()
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
