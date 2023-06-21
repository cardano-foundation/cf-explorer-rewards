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

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQPoolInfoRepositoryTest extends TestDataBaseContainer {

  @Autowired
  private DSLContext dsl;

  @Autowired
  private JOOQPoolInfoRepository jooqPoolInfoRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  private String tableName;
  private String fetchedAtEpochField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, PoolInfo.class);
    fetchedAtEpochField = entityUtil.getColumnField(PoolInfo_.FETCHED_AT_EPOCH);
    tableName = entityUtil.getTableName();
    dsl.deleteFrom(table(tableName)).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict unique field")
  void saveAll_shouldSaveSuccessWhenOnConflictUniqueField() {
    var poolInfo1 =
        PoolInfo.builder()
            .poolId(1L)
            .fetchedAtEpoch(315)
            .activeStake(BigInteger.valueOf(15233509666142L))
            .liveStake(BigInteger.valueOf(5122155516487L))
            .liveSaturation(21.08)
            .build();

    var poolInfo2 =
        PoolInfo.builder()
            .poolId(1L)
            .fetchedAtEpoch(315)
            .activeStake(BigInteger.valueOf(15233509666142L))
            .liveStake(BigInteger.valueOf(15122155516487L))
            .liveSaturation(21.08)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo2))),
            CompletableFuture.runAsync(
                () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo1))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(1, dsl.fetchCount(table(tableName)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not conflict unique field")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictUniqueField() {
    var poolInfo1 =
        PoolInfo.builder()
            .poolId(1L)
            .fetchedAtEpoch(315)
            .activeStake(BigInteger.valueOf(15233509666142L))
            .liveStake(BigInteger.valueOf(15122155516487L))
            .liveSaturation(21.08)
            .build();

    var poolInfo2 =
        PoolInfo.builder()
            .poolId(1L)
            .fetchedAtEpoch(316)
            .activeStake(BigInteger.valueOf(15233509666142L))
            .liveStake(BigInteger.valueOf(15122155516487L))
            .liveSaturation(21.08)
            .build();

    var poolInfo3 =
        PoolInfo.builder()
            .poolId(1L)
            .fetchedAtEpoch(317)
            .activeStake(BigInteger.valueOf(15233509666142L))
            .liveStake(BigInteger.valueOf(15122155516487L))
            .liveSaturation(21.08)
            .build();

    assertDoesNotThrow(
        () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo3)));
    assertEquals(3, dsl.fetchCount(table(tableName)));
    var response = dsl.fetch(table(tableName)).sortAsc(name(fetchedAtEpochField));
    assertEquals(315, response.get(0).get(field(fetchedAtEpochField)));
    assertEquals(316, response.get(1).get(field(fetchedAtEpochField)));
    assertEquals(317, response.get(2).get(field(fetchedAtEpochField)));
  }
}
