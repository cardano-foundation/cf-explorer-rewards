package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStake;
import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStake_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@Component
public class JOOQEpochStakeRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQEpochStakeRepository(
      DSLContext dsl, @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, EpochStake.class);
  }

  @Transactional
  public void saveAll(List<EpochStake> epochStakes) {
    if (epochStakes.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var epochNoField = entityUtil.getColumnField(EpochStake_.EPOCH_NO);
    var addrIdField = entityUtil.getColumnField(EpochStake_.STAKE_ADDRESS_ID);
    var poolIdField = entityUtil.getColumnField(EpochStake_.POOL_ID);
    var amountField = entityUtil.getColumnField(EpochStake_.AMOUNT);

    for (var epochStake : epochStakes) {
      Long addrId = (epochStake.getAddr() != null) ? epochStake.getAddr().getId() : null;
      Long poolId = (epochStake.getPool() != null) ? epochStake.getPool().getId() : null;
      Integer epochNo = epochStake.getEpochNo();
      BigInteger amount = epochStake.getAmount();
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(epochNoField), epochNo)
              .set(field(addrIdField), addrId)
              .set(field(poolIdField), poolId)
              .set(field(amountField), amount)
              .onConflict(field(epochNoField), field(addrIdField), field(poolIdField))
              .doNothing();

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
