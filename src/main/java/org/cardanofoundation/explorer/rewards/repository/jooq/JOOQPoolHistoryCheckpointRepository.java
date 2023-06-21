package org.cardanofoundation.explorer.rewards.repository.jooq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;
import org.jooq.Query;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class JOOQPoolHistoryCheckpointRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQPoolHistoryCheckpointRepository(DSLContext dsl,
                                             @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, PoolHistoryCheckpoint.class);
  }

  @Transactional
  public void saveAll(List<PoolHistoryCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var epochCheckpointField = entityUtil.getColumnField(PoolHistoryCheckpoint_.EPOCH_CHECKPOINT);
    var viewField = entityUtil.getColumnField(PoolHistoryCheckpoint_.VIEW);
    var isSpendableRewardField = entityUtil.getColumnField(
        PoolHistoryCheckpoint_.IS_SPENDABLE_REWARD);

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(viewField), checkpoint.getView())
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint())
              .set(field(isSpendableRewardField), checkpoint.getIsSpendableReward())
              .onConflict(field(viewField))
              .doUpdate()
              .set(field(epochCheckpointField), checkpoint.getEpochCheckpoint())
              .set(field(isSpendableRewardField), checkpoint.getIsSpendableReward());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
