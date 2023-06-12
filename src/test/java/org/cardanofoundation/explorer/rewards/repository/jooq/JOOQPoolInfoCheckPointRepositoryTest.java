package org.cardanofoundation.explorer.rewards.repository.jooq;

import static com.cardanofoundation.explorer.rewards.model.Tables.POOL_INFO_CHECKPOINT;
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

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfoCheckpoint;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQPoolInfoCheckPointRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQPoolInfoCheckpointRepository jooqPoolInfoCheckpointRepository;

  @BeforeEach
  void setUp() {
    dsl.deleteFrom(POOL_INFO_CHECKPOINT).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict view")
  void saveAll_shouldSaveSuccessWhenOnConflictView() {
    String poolView = "pool1vxz0deezj5c2950e7arpzfqxzq8zd9kawsullrzjw5rsq0yhxgr";
    var checkpoint1 = PoolInfoCheckpoint.builder().view(poolView).epochCheckpoint(414).build();
    // checkpoint2 has same stake address as checkpoint1
    var checkpoint2 = PoolInfoCheckpoint.builder().view(poolView).epochCheckpoint(415).build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqPoolInfoCheckpointRepository.saveAll(List.of(checkpoint1))),
            CompletableFuture.runAsync(
                () -> jooqPoolInfoCheckpointRepository.saveAll(List.of(checkpoint2))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(1, dsl.fetchCount(POOL_INFO_CHECKPOINT, POOL_INFO_CHECKPOINT.VIEW.eq(poolView)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not have conflict view")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictView() {
    var checkpoint1 = PoolInfoCheckpoint.builder().view("poolView1").epochCheckpoint(414).build();

    var checkpoint2 = PoolInfoCheckpoint.builder().view("poolView2").epochCheckpoint(414).build();

    var checkpoint3 = PoolInfoCheckpoint.builder().view("poolView3").epochCheckpoint(414).build();

    jooqPoolInfoCheckpointRepository.saveAll(List.of(checkpoint1, checkpoint2, checkpoint3));
    assertEquals(3, dsl.fetchCount(POOL_INFO_CHECKPOINT));
    assertEquals(
        1, dsl.fetchCount(POOL_INFO_CHECKPOINT, POOL_INFO_CHECKPOINT.VIEW.eq("poolView1")));
    assertEquals(
        1, dsl.fetchCount(POOL_INFO_CHECKPOINT, POOL_INFO_CHECKPOINT.VIEW.eq("poolView2")));
    assertEquals(
        1, dsl.fetchCount(POOL_INFO_CHECKPOINT, POOL_INFO_CHECKPOINT.VIEW.eq("poolView3")));
  }
}
