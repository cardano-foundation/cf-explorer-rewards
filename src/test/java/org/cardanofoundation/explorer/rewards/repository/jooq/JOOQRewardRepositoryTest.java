package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.REWARD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQRewardRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQRewardRepository jooqRewardRepository;

  @BeforeEach
  void setUp() {
    dsl.deleteFrom(REWARD).execute();
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

    assertEquals(2, dsl.fetchCount(REWARD));
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
    assertEquals(5, dsl.fetchCount(REWARD));
    var response = dsl.fetch(REWARD).sortAsc(REWARD.EARNED_EPOCH);
    assertEquals(410, response.get(0).getEarnedEpoch());
    assertEquals(411, response.get(1).getEarnedEpoch());
    assertEquals(412, response.get(2).getEarnedEpoch());
    assertEquals(413, response.get(3).getEarnedEpoch());
    assertEquals(414, response.get(4).getEarnedEpoch());
  }
}
