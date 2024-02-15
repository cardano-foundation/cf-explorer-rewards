package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.jooq.DSLContext;
import org.jooq.Query;

import org.cardanofoundation.explorer.common.entity.ledgersync.Reward;
import org.cardanofoundation.explorer.common.entity.ledgersync.Reward_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@Repository
public class JOOQRewardRepository {

  private final DSLContext dsl;

  private final EntityUtil entityUtil;

  public JOOQRewardRepository(
      DSLContext dsl, @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
    this.dsl = dsl;
    this.entityUtil = new EntityUtil(schema, Reward.class);
  }

  public void saveAll(List<Reward> rewards) {
    if (rewards.isEmpty()) {
      return;
    }
    var queries = new ArrayList<Query>();
    var addressIdField = entityUtil.getColumnField(Reward_.STAKE_ADDRESS_ID);
    var amountField = entityUtil.getColumnField(Reward_.AMOUNT);
    var typeField = entityUtil.getColumnField(Reward_.TYPE);
    var earnedEpochField = entityUtil.getColumnField(Reward_.EARNED_EPOCH);
    var spendableEpochField = entityUtil.getColumnField(Reward_.SPENDABLE_EPOCH);
    var poolIdField = entityUtil.getColumnField(Reward_.POOL_ID);

    for (var reward : rewards) {
      Long addrId = (reward.getAddr() != null) ? reward.getAddr().getId() : null;
      Long poolId = (reward.getPool() != null) ? reward.getPool().getId() : null;
      String type = reward.getType().getValue();
      Integer earnedEpoch = reward.getEarnedEpoch();
      Integer spendableEpoch = reward.getSpendableEpoch();
      BigInteger amount = reward.getAmount();
      var query =
          dsl.insertInto(table(entityUtil.getTableName()))
              .set(field(addressIdField), addrId)
              .set(field(amountField), amount)
              .set(field(typeField), type)
              .set(field(earnedEpochField), earnedEpoch)
              .set(field(spendableEpochField), spendableEpoch)
              .set(field(poolIdField), poolId)
              .onConflict(
                  field(addressIdField),
                  field(typeField),
                  field(earnedEpochField),
                  coalesce(field(poolIdField), -1))
              .doNothing();

      queries.add(query);
    }

    dsl.batch(queries).execute();
  }
}
