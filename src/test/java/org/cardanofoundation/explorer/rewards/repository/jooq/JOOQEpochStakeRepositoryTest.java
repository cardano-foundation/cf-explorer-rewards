package org.cardanofoundation.explorer.rewards.repository.jooq;


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

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.jooq.Name;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQEpochStakeRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQEpochStakeRepository jooqEpochStakeRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;
  private String tableName;
  private String epochNoField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, EpochStake.class);
    epochNoField = entityUtil.getColumnField(EpochStake_.EPOCH_NO);
    tableName = entityUtil.getTableName();
    dsl.deleteFrom(table(tableName)).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict unique field")
  void saveAll_shouldSaveSuccessWhenOnConflictUniqueField() {
    var stakeAddress =
        StakeAddress.builder()
            .id(1L)
            .view("stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls")
            .build();

    var poolHash =
        PoolHash.builder()
            .id(2L)
            .view("pool1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls")
            .build();

    var epochStake =
        EpochStake.builder()
            .epochNo(400)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var epochStake2 =
        EpochStake.builder()
            .epochNo(401)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () ->
                    jooqEpochStakeRepository.saveAll(
                        List.of(epochStake, epochStake2, epochStake2))),
            CompletableFuture.runAsync(
                () ->
                    jooqEpochStakeRepository.saveAll(
                        List.of(epochStake, epochStake2, epochStake))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(2, dsl.fetchCount(table(tableName)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not conflict unique field")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictUniqueField() {
    var stakeAddress =
        StakeAddress.builder()
            .id(1L)
            .view("stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls")
            .build();

    var poolHash =
        PoolHash.builder()
            .id(2L)
            .view("pool1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls")
            .build();

    var epochStake =
        EpochStake.builder()
            .epochNo(400)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var epochStake1 =
        EpochStake.builder()
            .epochNo(401)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var epochStake2 =
        EpochStake.builder()
            .epochNo(402)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var epochStake3 =
        EpochStake.builder()
            .epochNo(403)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var epochStake4 =
        EpochStake.builder()
            .epochNo(404)
            .amount(BigInteger.valueOf(1000000))
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    assertDoesNotThrow(
        () ->
            jooqEpochStakeRepository.saveAll(
                List.of(epochStake, epochStake1, epochStake2, epochStake3, epochStake4)));
    assertEquals(5, dsl.fetchCount(table(tableName)));
    var response = dsl.fetch(table(tableName)).sortAsc(name(epochNoField));

    assertEquals(400, response.get(0).get(field(epochNoField)));
    assertEquals(401, response.get(1).get(field(epochNoField)));
    assertEquals(402, response.get(2).get(field(epochNoField)));
    assertEquals(403, response.get(3).get(field(epochNoField)));
    assertEquals(404, response.get(4).get(field(epochNoField)));
  }
}