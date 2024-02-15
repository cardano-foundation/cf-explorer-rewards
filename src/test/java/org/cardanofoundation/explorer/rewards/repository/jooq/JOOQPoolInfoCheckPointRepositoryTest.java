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

import org.jooq.DSLContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolInfoCheckpoint_;
import org.cardanofoundation.explorer.common.utils.EntityUtil;

@JooqTest
@ActiveProfiles("integration-test")
@ComponentScan
class JOOQPoolInfoCheckPointRepositoryTest extends TestDataBaseContainer {

  @Autowired private DSLContext dsl;
  @Autowired private JOOQPoolInfoCheckpointRepository jooqPoolInfoCheckpointRepository;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String schema;

  private String tableName;
  private String viewField;

  @BeforeEach
  void setUp() {
    EntityUtil entityUtil = new EntityUtil(schema, PoolInfoCheckpoint.class);
    viewField = entityUtil.getColumnField(PoolInfoCheckpoint_.VIEW);
    tableName = entityUtil.getTableName();
    dsl.deleteFrom(table(tableName)).execute();
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

    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq(poolView)));
  }

  @Test
  @DisplayName("SaveAll should save success when does not have conflict view")
  void saveAll_shouldSaveSuccessWhenDoesNotOnConflictView() {
    var checkpoint1 = PoolInfoCheckpoint.builder().view("poolView1").epochCheckpoint(414).build();

    var checkpoint2 = PoolInfoCheckpoint.builder().view("poolView2").epochCheckpoint(414).build();

    var checkpoint3 = PoolInfoCheckpoint.builder().view("poolView3").epochCheckpoint(414).build();

    jooqPoolInfoCheckpointRepository.saveAll(List.of(checkpoint1, checkpoint2, checkpoint3));
    assertEquals(3, dsl.fetchCount(table(tableName)));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("poolView1")));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("poolView2")));
    assertEquals(1, dsl.fetchCount(table(tableName), field(viewField).eq("poolView3")));
  }
}
