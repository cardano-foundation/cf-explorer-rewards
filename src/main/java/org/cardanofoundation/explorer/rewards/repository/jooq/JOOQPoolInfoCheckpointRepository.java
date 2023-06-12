package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_INFO_CHECKPOINT;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;

@Component
@RequiredArgsConstructor
public class JOOQPoolInfoCheckpointRepository {

  private final DSLContext dsl;

  public void saveAll(List<PoolInfoCheckpoint> checkpoints) {
    var queries = new ArrayList<Query>();

    for (var checkpoint : checkpoints) {
      var query =
          dsl.insertInto(
                  POOL_INFO_CHECKPOINT,
                  POOL_INFO_CHECKPOINT.VIEW,
                  POOL_INFO_CHECKPOINT.EPOCH_CHECKPOINT)
              .values(checkpoint.getView(), checkpoint.getEpochCheckpoint())
              .onConflict(POOL_INFO_CHECKPOINT.VIEW)
              .doUpdate()
              .set(POOL_INFO_CHECKPOINT.EPOCH_CHECKPOINT, checkpoint.getEpochCheckpoint());
      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
