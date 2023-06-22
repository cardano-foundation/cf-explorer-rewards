package org.cardanofoundation.explorer.rewards.repository.jooq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class JOOQRewardCheckpointRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQRewardCheckpointRepository(DSLContext dsl,
                                        @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, RewardCheckpoint.class);
  }


  public void saveAll(List<RewardCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var epochCheckpointField = entityUtil.getColumnField(RewardCheckpoint_.EPOCH_CHECKPOINT);
    var stakeAddressField = entityUtil.getColumnField(RewardCheckpoint_.STAKE_ADDRESS);

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
