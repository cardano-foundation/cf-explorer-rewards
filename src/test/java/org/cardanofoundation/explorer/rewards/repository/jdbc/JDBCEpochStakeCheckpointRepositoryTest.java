package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.test.context.ActiveProfiles;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JDBCEpochStakeCheckpointRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCEpochStakeCheckpointRepository jdbcEpochStakeCheckpointRepository;

  private final int batchSize = 100;

  @BeforeEach
  void setUp() {
    jdbcEpochStakeCheckpointRepository = new JDBCEpochStakeCheckpointRepository(
        jdbcTemplate, batchSize);
  }

  @Test
  void testSaveAll() {
    // Setup
    var checkpoint1 = new EpochStakeCheckpoint(
        "stake1uxgfzz027y0scn8pqh220vk08s0nc74plnrl6wmr5nve2lqt5mfls",
        414);
    var checkpoint2 = new EpochStakeCheckpoint(
        "stake1u8ludg6vutasqf7sfzwp73tw4ukszy7kwqfk7hd52yq9frs528563",
        414);

    final List<EpochStakeCheckpoint> epochStakeCheckpoints = List.of(checkpoint2, checkpoint1);

    // Run the test
    jdbcEpochStakeCheckpointRepository.saveAll(epochStakeCheckpoints);

    // Verify the results
    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO epoch_stake_checkpoint (id, view, epoch_checkpoint)  VALUES (nextval('epoch_stake_checkpoint_id_seq'), ?, ?)    ON CONFLICT (view) DO NOTHING"),
        eq(epochStakeCheckpoints), eq(batchSize),
        any(ParameterizedPreparedStatementSetter.class));
  }
}
