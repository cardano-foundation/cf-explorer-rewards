package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.EPOCH_STAKE_CHECKPOINT;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;

@Component
@RequiredArgsConstructor
public class JOOQEpochStakeCheckpointRepository {

  private final DSLContext dsl;

  @Transactional
  public void saveAll(List<EpochStakeCheckpoint> checkpoints) {
    if (checkpoints.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(
                  EPOCH_STAKE_CHECKPOINT,
                  EPOCH_STAKE_CHECKPOINT.VIEW,
                  EPOCH_STAKE_CHECKPOINT.EPOCH_CHECKPOINT)
              .values(checkpoint.getStakeAddress(), checkpoint.getEpochCheckpoint())
              .onConflict(EPOCH_STAKE_CHECKPOINT.VIEW)
              .doUpdate()
              .set(EPOCH_STAKE_CHECKPOINT.EPOCH_CHECKPOINT, checkpoint.getEpochCheckpoint());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
