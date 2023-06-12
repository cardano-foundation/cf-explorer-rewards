package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.REWARD;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.Reward;

@Component
@RequiredArgsConstructor
public class JOOQRewardRepository {

  private final DSLContext dsl;

  public void saveAll(List<Reward> rewards) {
    if (rewards.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var reward : rewards) {
      Long addrId = (reward.getAddr() != null) ? reward.getAddr().getId() : null;
      Long poolId = (reward.getPool() != null) ? reward.getPool().getId() : null;
      String type = reward.getType().getValue();
      Long earnedEpoch = reward.getEarnedEpoch().longValue();
      Long spendableEpoch = reward.getSpendableEpoch().longValue();
      Long amount = reward.getAmount().longValue();
      var query =
          dsl.insertInto(
                  REWARD,
                  REWARD.ADDR_ID,
                  REWARD.AMOUNT,
                  REWARD.TYPE,
                  REWARD.EARNED_EPOCH,
                  REWARD.SPENDABLE_EPOCH,
                  REWARD.POOL_ID)
              .values(addrId, amount, type, earnedEpoch, spendableEpoch, poolId)
              .onConflict(REWARD.ADDR_ID, REWARD.TYPE, REWARD.EARNED_EPOCH, REWARD.POOL_ID)
              .doNothing();

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}

