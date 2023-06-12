package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.EPOCH_STAKE_CHECKPOINT;
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

import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;


@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQEpochStakeCheckPointRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQEpochStakeCheckpointRepository jooqEpochStakeCheckpointRepository;

  @BeforeEach
  void setUp() {
    dsl.deleteFrom(EPOCH_STAKE_CHECKPOINT).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict view")
  void saveAll_shouldSaveSuccessWhenOnConflictView() {
    String stakeAddress = "stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls";
    var checkpoint1 =
        EpochStakeCheckpoint.builder().stakeAddress(stakeAddress).epochCheckpoint(414).build();
    // checkpoint2 has same stake address as checkpoint1
    var checkpoint2 =
        EpochStakeCheckpoint.builder().stakeAddress(stakeAddress).epochCheckpoint(415).build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqEpochStakeCheckpointRepository.saveAll(List.of(checkpoint1))),
            CompletableFuture.runAsync(
                () -> jooqEpochStakeCheckpointRepository.saveAll(List.of(checkpoint2))));

    completableFutures.forEach(CompletableFuture::join);

    dsl.fetch(EPOCH_STAKE_CHECKPOINT, EPOCH_STAKE_CHECKPOINT.VIEW.eq(stakeAddress));
    assertEquals(
        1, dsl.fetchCount(EPOCH_STAKE_CHECKPOINT, EPOCH_STAKE_CHECKPOINT.VIEW.eq(stakeAddress)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not have conflict view")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictView() {
    var checkpoint1 =
        EpochStakeCheckpoint.builder().stakeAddress("stakeKey1").epochCheckpoint(414).build();
    var checkpoint2 =
        EpochStakeCheckpoint.builder().stakeAddress("stakeKey2").epochCheckpoint(415).build();
    var checkpoint3 =
        EpochStakeCheckpoint.builder().stakeAddress("stakeKey3").epochCheckpoint(412).build();

    jooqEpochStakeCheckpointRepository.saveAll(List.of(checkpoint1, checkpoint2, checkpoint3));
    assertEquals(3, dsl.fetchCount(EPOCH_STAKE_CHECKPOINT));
    assertEquals(
        1, dsl.fetchCount(EPOCH_STAKE_CHECKPOINT, EPOCH_STAKE_CHECKPOINT.VIEW.eq("stakeKey1")));
    assertEquals(
        1, dsl.fetchCount(EPOCH_STAKE_CHECKPOINT, EPOCH_STAKE_CHECKPOINT.VIEW.eq("stakeKey2")));
    assertEquals(
        1, dsl.fetchCount(EPOCH_STAKE_CHECKPOINT, EPOCH_STAKE_CHECKPOINT.VIEW.eq("stakeKey3")));
  }
}
