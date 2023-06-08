package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistoryCheckpoint;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JDBCPoolHistoryCheckpointRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCPoolHistoryCheckpointRepository jdbcPoolHistoryCheckpointRepository;

  private final int batchSize = 100;

  @BeforeEach
  void setUp() {
    jdbcPoolHistoryCheckpointRepository = new JDBCPoolHistoryCheckpointRepository(
        jdbcTemplate, batchSize);
  }

  @Test
  void testSaveAll() {
    var checkpoint1 = new PoolHistoryCheckpoint(
        "pool1dmqzwuql5mylffvn7ln3pr9j7kh4gdsssrmma5wgx56f6rtyf42",
        414);
    var checkpoint2 = new PoolHistoryCheckpoint(
        "pool1qzlw7z5mutmd39ldyjnp8n650weqe55z5p8dl3fagac3ge0nx8l",
        414);

    final List<PoolHistoryCheckpoint> poolHistoryCheckpoints = List.of(checkpoint1, checkpoint2);

    jdbcPoolHistoryCheckpointRepository.saveAll(poolHistoryCheckpoints);

    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO pool_history_checkpoint (id, view, epoch_checkpoint)  VALUES (nextval('pool_history_checkpoint_id_seq'), ?, ?)    ON CONFLICT (view) DO NOTHING"),
        eq(poolHistoryCheckpoints), eq(batchSize),
        any(ParameterizedPreparedStatementSetter.class));
  }
}
