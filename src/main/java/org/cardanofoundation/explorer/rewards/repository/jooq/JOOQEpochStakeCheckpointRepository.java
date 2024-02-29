package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStakeCheckpoint;
import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStakeCheckpoint_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@Component
public class JOOQEpochStakeCheckpointRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQEpochStakeCheckpointRepository(
      DSLContext dsl, @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, EpochStakeCheckpoint.class);
  }

  @Transactional
  public void saveAll(List<EpochStakeCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var epochCheckpointField = entityUtil.getColumnField(EpochStakeCheckpoint_.EPOCH_CHECKPOINT);
    var stakeAddressField = entityUtil.getColumnField(EpochStakeCheckpoint_.STAKE_ADDRESS);

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(stakeAddressField), checkpoint.getStakeAddress())
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint())
              .onConflict(field(stakeAddressField))
              .doUpdate()
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
