package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.EPOCH_STAKE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;

@Component
@RequiredArgsConstructor
public class JOOQEpochStakeRepository {

  private final DSLContext dsl;

  @Transactional
  public void saveAll(List<EpochStake> epochStakes) {
    if (epochStakes.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();

    for (var epochStake : epochStakes) {
      Long addrId = (epochStake.getAddr() != null) ? epochStake.getAddr().getId() : null;
      Long poolId = (epochStake.getPool() != null) ? epochStake.getPool().getId() : null;
      Integer epochNo = epochStake.getEpochNo();
      BigDecimal amount = new BigDecimal(epochStake.getAmount());
      var query =
          dsl.insertInto(
                  EPOCH_STAKE,
                  EPOCH_STAKE.EPOCH_NO,
                  EPOCH_STAKE.ADDR_ID,
                  EPOCH_STAKE.POOL_ID,
                  EPOCH_STAKE.AMOUNT)
              .values(epochNo, addrId, poolId, amount)
              .onConflict(EPOCH_STAKE.EPOCH_NO, EPOCH_STAKE.ADDR_ID, EPOCH_STAKE.POOL_ID)
              .doNothing();

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}