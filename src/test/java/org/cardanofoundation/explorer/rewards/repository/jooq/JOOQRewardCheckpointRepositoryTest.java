package org.cardanofoundation.explorer.rewards.repository.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint_;
import org.cardanofoundation.explorer.rewards.util.EntityUtil;
import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;


@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQRewardCheckpointRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;

  @Autowired private JOOQRewardCheckpointRepository jooqRewardCheckpointRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  private String tableName;
  private String viewField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, RewardCheckpoint.class);
    viewField = entityUtil.getColumnField(RewardCheckpoint_.STAKE_ADDRESS);
    tableName = entityUtil.getTableName();
    dsl.deleteFrom(table(tableName)).execute();
  }

  @Test
  @DisplayName("SaveAll should save success when on conflict view")
  void saveAll_shouldSaveSuccessWhenOnConflictView() {
    String stakeAddress = "stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls";
    var checkpoint1 =
        RewardCheckpoint.builder().stakeAddress(stakeAddress).epochCheckpoint(414).build();
    // checkpoint2 has same stake address as checkpoint1
    var checkpoint2 =
        RewardCheckpoint.builder().stakeAddress(stakeAddress).epochCheckpoint(415).build();

    List<CompletableFuture<Void>> completableFutures =
        List.of(
            CompletableFuture.runAsync(
                () -> jooqRewardCheckpointRepository.saveAll(List.of(checkpoint1))),
            CompletableFuture.runAsync(
                () -> jooqRewardCheckpointRepository.saveAll(List.of(checkpoint2))));

    completableFutures.forEach(CompletableFuture::join);

    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq(stakeAddress)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not have conflict view")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictView() {
    var checkpoint1 =
        RewardCheckpoint.builder().stakeAddress("stakeKey1").epochCheckpoint(414).build();
    var checkpoint2 =
        RewardCheckpoint.builder().stakeAddress("stakeKey2").epochCheckpoint(415).build();
    var checkpoint3 =
        RewardCheckpoint.builder().stakeAddress("stakeKey3").epochCheckpoint(412).build();

    jooqRewardCheckpointRepository.saveAll(List.of(checkpoint1, checkpoint2, checkpoint3));
    assertEquals(3, dsl.fetchCount(table(tableName)));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("stakeKey1")));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("stakeKey2")));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("stakeKey3")));
  }
}
