package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfo;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfo_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@Component
public class JOOQPoolInfoRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQPoolInfoRepository(
      DSLContext dsl, @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, PoolInfo.class);
  }

  public void saveAll(List<PoolInfo> poolInfos) {
    if (poolInfos.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var poolIdField = entityUtil.getColumnField(PoolInfo_.POOL_ID);
    var fetchedAtEpochField = entityUtil.getColumnField(PoolInfo_.FETCHED_AT_EPOCH);
    var activeStakeField = entityUtil.getColumnField(PoolInfo_.ACTIVE_STAKE);
    var liveStakeField = entityUtil.getColumnField(PoolInfo_.LIVE_STAKE);
    var liveSaturationField = entityUtil.getColumnField(PoolInfo_.LIVE_SATURATION);

    for (var poolInfo : poolInfos) {
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(poolIdField), poolInfo.getPoolId())
              .set(field(fetchedAtEpochField), poolInfo.getFetchedAtEpoch())
              .set(field(activeStakeField), poolInfo.getActiveStake())
              .set(field(liveStakeField), poolInfo.getLiveStake())
              .set(field(liveSaturationField), poolInfo.getLiveSaturation())
              .onConflict(field(poolIdField), field(fetchedAtEpochField))
              .doUpdate()
              .set(field(activeStakeField), poolInfo.getActiveStake())
              .set(field(liveStakeField), poolInfo.getLiveStake())
              .set(field(liveSaturationField), poolInfo.getLiveSaturation());

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
