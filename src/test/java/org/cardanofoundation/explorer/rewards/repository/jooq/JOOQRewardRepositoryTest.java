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

import org.cardanofoundation.explorer.common.entity.enumeration.RewardType;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHash;
import org.cardanofoundation.explorer.common.entity.ledgersync.Reward;
import org.cardanofoundation.explorer.common.entity.ledgersync.Reward_;
import org.cardanofoundation.explorer.common.entity.ledgersync.StakeAddress;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQRewardRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQRewardRepository jooqRewardRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  private String tableName;
  String earnedEpochField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, Reward.class);
    earnedEpochField = entityUtil.getColumnField(Reward_.EARNED_EPOCH);
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

    var reward =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(414)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var reward2 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(410)
            .spendableEpoch(415)
            .type(RewardType.LEADER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqRewardRepository.saveAll(List.of(reward, reward2, reward2))),
            CompletableFuture.runAsync(
                () -> jooqRewardRepository.saveAll(List.of(reward, reward2, reward))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(2, dsl.fetchCount(table(tableName)));
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict unique field")
  void saveAll_shouldSaveSuccessWhenOnConflictUniqueFieldAndPooIdNull() {
    var stakeAddress =
        StakeAddress.builder()
            .id(1L)
            .view("stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls")
            .build();

    var reward =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(414)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(null)
            .build();

    var reward2 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(414)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(null)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqRewardRepository.saveAll(List.of(reward, reward2, reward2))),
            CompletableFuture.runAsync(
                () -> jooqRewardRepository.saveAll(List.of(reward, reward2, reward))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(1, dsl.fetchCount(table(tableName)));
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

    var reward =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(411)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var reward2 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(412)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var reward3 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(413)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var reward4 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(414)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    var reward5 =
        Reward.builder()
            .amount(BigInteger.valueOf(1000000))
            .earnedEpoch(410)
            .spendableEpoch(415)
            .type(RewardType.MEMBER)
            .addr(stakeAddress)
            .pool(poolHash)
            .build();

    assertDoesNotThrow(
        () -> jooqRewardRepository.saveAll(List.of(reward, reward2, reward3, reward4, reward5)));
    assertEquals(5, dsl.fetchCount(table(tableName)));
    var response = dsl.fetch(table(tableName)).sortAsc(name(earnedEpochField));
    assertEquals(410L, response.get(0).get(field(earnedEpochField)));
    assertEquals(411L, response.get(1).get(field(earnedEpochField)));
    assertEquals(412L, response.get(2).get(field(earnedEpochField)));
    assertEquals(413L, response.get(3).get(field(earnedEpochField)));
    assertEquals(414L, response.get(4).get(field(earnedEpochField)));
  }
}
