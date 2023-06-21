package org.cardanofoundation.explorer.rewards.repository.jooq;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;
import org.jooq.Query;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class JOOQPoolHistoryRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQPoolHistoryRepository(DSLContext dsl,
                                   @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, PoolHistory.class);
  }

  public void saveAll(List<PoolHistory> poolHistories) {
    if (poolHistories.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var poolIdField = entityUtil.getColumnField(PoolHistory_.POOL_ID);
    var epochNoField = entityUtil.getColumnField(PoolHistory_.EPOCH_NO);
    var activeStakeField = entityUtil.getColumnField(PoolHistory_.ACTIVE_STAKE);
    var activeStakePctField = entityUtil.getColumnField(PoolHistory_.ACTIVE_STAKE_PCT);
    var saturationPctField = entityUtil.getColumnField(PoolHistory_.SATURATION_PCT);
    var blockCntField = entityUtil.getColumnField(PoolHistory_.BLOCK_CNT);
    var delegatorCntField = entityUtil.getColumnField(PoolHistory_.DELEGATOR_CNT);
    var marginField = entityUtil.getColumnField(PoolHistory_.MARGIN);
    var fixedCostField = entityUtil.getColumnField(PoolHistory_.FIXED_COST);
    var poolFeesField = entityUtil.getColumnField(PoolHistory_.POOL_FEES);
    var delegRewardsField = entityUtil.getColumnField(PoolHistory_.DELEGATOR_REWARDS);
    var epochRosField = entityUtil.getColumnField(PoolHistory_.EPOCH_ROS);

    for (var poolHistory : poolHistories) {
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(poolIdField), poolHistory.getPoolId())
              .set(field(epochNoField), poolHistory.getEpochNo())
              .set(field(activeStakeField), poolHistory.getActiveStake())
              .set(field(activeStakePctField), poolHistory.getActiveStakePct())
              .set(field(saturationPctField), poolHistory.getSaturationPct())
              .set(field(blockCntField), poolHistory.getBlockCnt())
              .set(field(delegatorCntField), poolHistory.getDelegatorCnt())
              .set(field(marginField), poolHistory.getMargin())
              .set(field(fixedCostField), poolHistory.getFixedCost())
              .set(field(poolFeesField), poolHistory.getPoolFees())
              .set(field(delegRewardsField), poolHistory.getDelegatorRewards())
              .set(field(epochRosField), poolHistory.getEpochRos())
              .onConflict(field(poolIdField), field(epochNoField))
              .doUpdate()
              .set(field(poolFeesField), poolHistory.getPoolFees())
              .set(field(delegRewardsField), poolHistory.getDelegatorRewards())
              .set(field(epochRosField), poolHistory.getEpochRos());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}

