package org.cardanofoundation.explorer.rewards.repository.jooq;


import static com.cardanofoundation.explorer.rewards.model.Tables.EPOCH_STAKE;
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

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQEpochStakeRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQEpochStakeRepository jooqEpochStakeRepository;

  @BeforeEach
  void setUp() {
    dsl.deleteFrom(EPOCH_STAKE).execute();
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

    assertEquals(2, dsl.fetchCount(EPOCH_STAKE));
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
    assertEquals(5, dsl.fetchCount(EPOCH_STAKE));
    var response = dsl.fetch(EPOCH_STAKE).sortAsc(EPOCH_STAKE.EPOCH_NO);
    assertEquals(400, response.get(0).getEpochNo());
    assertEquals(401, response.get(1).getEpochNo());
    assertEquals(402, response.get(2).getEpochNo());
    assertEquals(403, response.get(3).getEpochNo());
    assertEquals(404, response.get(4).getEpochNo());
  }
}