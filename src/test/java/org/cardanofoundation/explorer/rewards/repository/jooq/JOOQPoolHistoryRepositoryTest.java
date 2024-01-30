package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQPoolHistoryRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQPoolHistoryRepository jooqPoolHistoryRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  private String tableName;
  private String epochNoField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, PoolHistory.class);
    epochNoField = entityUtil.getColumnField(PoolHistory_.EPOCH_NO);
    tableName = entityUtil.getTableName();
    dsl.deleteFrom(table(tableName)).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict unique field")
  void saveAll_shouldSaveSuccessWhenOnConflictUniqueField() {
    var poolHistory1 =
        PoolHistory.builder()
            .poolId(1L)
            .epochNo(314)
            .activeStake(BigInteger.valueOf(158530782))
            .activeStakePct(0.0000006715986583)
            .blockCnt(0)
            .delegatorCnt(4)
            .margin(0.0)
            .fixedCost(BigInteger.valueOf(340000000))
            .poolFees(BigInteger.valueOf(0))
            .delegatorRewards(BigInteger.valueOf(0))
            .epochRos(0.0)
            .build();

    var poolHistory2 =
        PoolHistory.builder()
            .poolId(1L)
            .epochNo(315)
            .activeStake(BigInteger.valueOf(1223213))
            .activeStakePct(0.0000006715986583)
            .blockCnt(0)
            .delegatorCnt(100)
            .margin(0.0)
            .fixedCost(BigInteger.valueOf(340000000))
            .poolFees(BigInteger.valueOf(123444))
            .delegatorRewards(BigInteger.valueOf(0))
            .epochRos(0.0)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () ->
                    jooqPoolHistoryRepository.saveAll(
                        List.of(poolHistory1, poolHistory2, poolHistory2))),
            CompletableFuture.runAsync(
                () ->
                    jooqPoolHistoryRepository.saveAll(
                        List.of(poolHistory1, poolHistory2, poolHistory1))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(2, dsl.fetchCount(table(tableName)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not conflict unique field")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictUniqueField() {
    var poolHistory1 =
        PoolHistory.builder()
            .poolId(1L)
            .epochNo(314)
            .activeStake(BigInteger.valueOf(158530782))
            .activeStakePct(0.0000006715986583)
            .blockCnt(0)
            .delegatorCnt(4)
            .margin(0.0)
            .fixedCost(BigInteger.valueOf(340000000))
            .poolFees(BigInteger.valueOf(0))
            .delegatorRewards(BigInteger.valueOf(0))
            .epochRos(0.0)
            .build();

    var poolHistory2 =
        PoolHistory.builder()
            .poolId(1L)
            .epochNo(315)
            .activeStake(BigInteger.valueOf(1223213))
            .activeStakePct(0.0000006715986583)
            .blockCnt(0)
            .delegatorCnt(100)
            .margin(0.0)
            .fixedCost(BigInteger.valueOf(340000000))
            .poolFees(BigInteger.valueOf(123444))
            .delegatorRewards(BigInteger.valueOf(0))
            .epochRos(0.0)
            .build();

    var poolHistory3 =
        PoolHistory.builder()
            .poolId(1L)
            .epochNo(316)
            .activeStake(BigInteger.valueOf(1223213))
            .activeStakePct(0.0000006715986583)
            .blockCnt(0)
            .delegatorCnt(100)
            .margin(0.0)
            .fixedCost(BigInteger.valueOf(340000000))
            .poolFees(BigInteger.valueOf(123444))
            .delegatorRewards(BigInteger.valueOf(0))
            .epochRos(0.0)
            .build();

    assertDoesNotThrow(
        () -> jooqPoolHistoryRepository.saveAll(List.of(poolHistory1, poolHistory2, poolHistory3)));
    assertEquals(3, dsl.fetchCount(table(tableName)));
    var response = dsl.fetch(table(tableName)).sortAsc(name(epochNoField));
    assertEquals(314, response.get(0).get(field(epochNoField)));
    assertEquals(315, response.get(1).get(field(epochNoField)));
    assertEquals(316, response.get(2).get(field(epochNoField)));
  }
}
