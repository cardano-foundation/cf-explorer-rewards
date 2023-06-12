package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_INFO;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;

@Component
@RequiredArgsConstructor
public class JOOQPoolInfoRepository {

  private final DSLContext dsl;

  public void saveAll(List<PoolInfo> poolInfos) {
    if (poolInfos.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var poolInfo : poolInfos) {
      var query =
          dsl.insertInto(
                  POOL_INFO,
                  POOL_INFO.POOL_ID,
                  POOL_INFO.FETCHED_AT_EPOCH,
                  POOL_INFO.ACTIVE_STAKE,
                  POOL_INFO.LIVE_STAKE,
                  POOL_INFO.LIVE_SATURATION)
              .values(
                  poolInfo.getPoolId(),
                  poolInfo.getFetchedAtEpoch(),
                  poolInfo.getActiveStake(),
                  poolInfo.getLiveStake(),
                  poolInfo.getLiveSaturation())
              .onConflict(POOL_INFO.POOL_ID, POOL_INFO.FETCHED_AT_EPOCH)
              .doUpdate()
              .set(POOL_INFO.ACTIVE_STAKE, poolInfo.getActiveStake())
              .set(POOL_INFO.LIVE_STAKE, poolInfo.getLiveStake())
              .set(POOL_INFO.LIVE_SATURATION, poolInfo.getLiveSaturation());

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
