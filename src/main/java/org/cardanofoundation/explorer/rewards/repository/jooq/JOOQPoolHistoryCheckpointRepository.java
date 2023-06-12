package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_HISTORY_CHECKPOINT;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;

@Component
@RequiredArgsConstructor
public class JOOQPoolHistoryCheckpointRepository {

  private final DSLContext dsl;

  @Transactional
  public void saveAll(List<PoolHistoryCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(
                  POOL_HISTORY_CHECKPOINT,
                  POOL_HISTORY_CHECKPOINT.VIEW,
                  POOL_HISTORY_CHECKPOINT.EPOCH_CHECKPOINT)
              .values(checkpoint.getView(), checkpoint.getEpochCheckpoint())
              .onConflict(POOL_HISTORY_CHECKPOINT.VIEW)
              .doUpdate()
              .set(POOL_HISTORY_CHECKPOINT.IS_SPENDABLE_REWARD, checkpoint.getIsSpendableReward());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
