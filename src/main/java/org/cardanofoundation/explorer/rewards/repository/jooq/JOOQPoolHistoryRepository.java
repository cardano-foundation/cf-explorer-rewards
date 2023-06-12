package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_HISTORY;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;

@Component
@RequiredArgsConstructor
public class JOOQPoolHistoryRepository {

  private final DSLContext dsl;

  public void saveAll(List<PoolHistory> poolHistories) {
    if (poolHistories.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var poolHistory : poolHistories) {
      var query =
          dsl.insertInto(
                  POOL_HISTORY,
                  POOL_HISTORY.POOL_ID,
                  POOL_HISTORY.EPOCH_NO,
                  POOL_HISTORY.ACTIVE_STAKE,
                  POOL_HISTORY.ACTIVE_STAKE_PCT,
                  POOL_HISTORY.SATURATION_PCT,
                  POOL_HISTORY.BLOCK_CNT,
                  POOL_HISTORY.DELEGATOR_CNT,
                  POOL_HISTORY.MARGIN,
                  POOL_HISTORY.FIXED_COST,
                  POOL_HISTORY.POOL_FEES,
                  POOL_HISTORY.DELEG_REWARDS,
                  POOL_HISTORY.EPOCH_ROS)
              .values(
                  poolHistory.getPoolId(),
                  poolHistory.getEpochNo(),
                  poolHistory.getActiveStake(),
                  poolHistory.getActiveStakePct(),
                  poolHistory.getSaturationPct(),
                  poolHistory.getBlockCnt(),
                  poolHistory.getDelegatorCnt(),
                  BigDecimal.valueOf(poolHistory.getMargin()),
                  poolHistory.getFixedCost(),
                  poolHistory.getPoolFees(),
                  poolHistory.getDelegatorRewards(),
                  BigDecimal.valueOf(poolHistory.getEpochRos()))
              .onConflict(POOL_HISTORY.POOL_ID, POOL_HISTORY.EPOCH_NO)
              .doUpdate()
              .set(POOL_HISTORY.POOL_FEES, poolHistory.getPoolFees())
              .set(POOL_HISTORY.DELEG_REWARDS, poolHistory.getDelegatorRewards())
              .set(POOL_HISTORY.EPOCH_ROS, BigDecimal.valueOf(poolHistory.getEpochRos()));

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}

