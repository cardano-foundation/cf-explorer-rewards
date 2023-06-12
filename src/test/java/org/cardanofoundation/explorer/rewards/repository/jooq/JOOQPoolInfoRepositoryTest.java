package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_INFO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQPoolInfoRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQPoolInfoRepository jooqPoolInfoRepository;

  @BeforeEach
  void setUp() {
    dsl.deleteFrom(POOL_INFO).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict unique field")
  void saveAll_shouldSaveSuccessWhenOnConflictUniqueField() {
    var poolInfo1 =
        PoolInfo.builder()
            .poolId("pool1gmwp956mrfn5hcs7rp7nasa7xpwmx84kkkclyx48xwuh5dltmpj")
            .fetchedAtEpoch(315)
            .activeStake("15233509666142")
            .liveStake("15122155516487")
            .liveSaturation(21.08)
            .build();

    var poolInfo2 =
        PoolInfo.builder()
            .poolId("pool1gmwp956mrfn5hcs7rp7nasa7xpwmx84kkkclyx48xwuh5dltmpj")
            .fetchedAtEpoch(316)
            .activeStake("15233509666142")
            .liveStake("15122155516487")
            .liveSaturation(21.08)
            .build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo2))),
            CompletableFuture.runAsync(
                () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo1))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(2, dsl.fetchCount(POOL_INFO));
  }

  @Test
  @DisplayName("SaveAll should save success when does not conflict unique field")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictUniqueField() {
    var poolInfo1 =
        PoolInfo.builder()
            .poolId("pool1gmwp956mrfn5hcs7rp7nasa7xpwmx84kkkclyx48xwuh5dltmpj")
            .fetchedAtEpoch(315)
            .activeStake("15233509666142")
            .liveStake("15122155516487")
            .liveSaturation(21.08)
            .build();

    var poolInfo2 =
        PoolInfo.builder()
            .poolId("pool1gmwp956mrfn5hcs7rp7nasa7xpwmx84kkkclyx48xwuh5dltmpj")
            .fetchedAtEpoch(316)
            .activeStake("15233509666142")
            .liveStake("15122155516487")
            .liveSaturation(21.08)
            .build();

    var poolInfo3 =
        PoolInfo.builder()
            .poolId("pool1gmwp956mrfn5hcs7rp7nasa7xpwmx84kkkclyx48xwuh5dltmpj")
            .fetchedAtEpoch(317)
            .activeStake("15233509666142")
            .liveStake("15122155516487")
            .liveSaturation(21.08)
            .build();

    assertDoesNotThrow(
        () -> jooqPoolInfoRepository.saveAll(List.of(poolInfo1, poolInfo2, poolInfo3)));
    assertEquals(3, dsl.fetchCount(POOL_INFO));
    var response = dsl.fetch(POOL_INFO).sortAsc(POOL_INFO.FETCHED_AT_EPOCH);
    assertEquals(315, response.get(0).getFetchedAtEpoch());
    assertEquals(316, response.get(1).getFetchedAtEpoch());
    assertEquals(317, response.get(2).getFetchedAtEpoch());
  }
}
