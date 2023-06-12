package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.REWARD_CHECKPOINT;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;

@Component
@RequiredArgsConstructor
public class JOOQRewardCheckpointRepository {

  private final DSLContext dsl;

  public void saveAll(List<RewardCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(
                  REWARD_CHECKPOINT, REWARD_CHECKPOINT.VIEW, REWARD_CHECKPOINT.EPOCH_CHECKPOINT)
              .values(checkpoint.getStakeAddress(), checkpoint.getEpochCheckpoint())
              .onConflict(REWARD_CHECKPOINT.VIEW)
              .doUpdate()
              .set(REWARD_CHECKPOINT.EPOCH_CHECKPOINT, checkpoint.getEpochCheckpoint());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}