package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHistory;
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
class JDBCPoolHistoryRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCPoolHistoryRepository jdbcPoolHistoryRepository;

  private final int batchSize = 100;

  @BeforeEach
  void setUp() {
    jdbcPoolHistoryRepository = new JDBCPoolHistoryRepository(jdbcTemplate, batchSize);
  }

  @Test
  void testSaveAll() {
    // Setup
    final var poolHistory1 = PoolHistory.builder()
        .epochNo(414)
        .activeStake("64328992063709")
        .activeStakePct(0.28853492634670874)
        .blockCnt(56)
        .poolFees("0")
        .delegatorCnt(2)
        .delegRewards("0")
        .epochRos(0.0)
        .fixedCost("500000000")
        .margin(1.0)
        .saturationPct(89.78)
        .poolId("pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc")
        .build();

    final var poolHistory2 = PoolHistory.builder()
        .epochNo(400)
        .activeStake("64328992063709")
        .activeStakePct(0.2398534926346874)
        .blockCnt(60)
        .poolFees("0")
        .delegatorCnt(2)
        .delegRewards("0")
        .epochRos(0.0)
        .fixedCost("500000000")
        .margin(1.0)
        .saturationPct(79.78)
        .poolId("pool155efqn9xpcf73pphkk88cmlkdwx4ulkg606tne970qswczg3asc")
        .build();

    final List<PoolHistory> poolHistoryList = List.of(poolHistory1, poolHistory2);

    jdbcPoolHistoryRepository.saveAll(poolHistoryList);

    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO pool_history (id, pool_id, epoch_no, active_stake, active_stake_pct, "
            + "saturation_pct, block_cnt, delegator_cnt, margin, fixed_cost, pool_fees, deleg_rewards, epoch_ros)"
            + " VALUES (nextval('pool_history_id_seq'),?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  "
            + "ON CONFLICT (pool_id, epoch_no) DO NOTHING"),
        eq(poolHistoryList), eq(batchSize), any(ParameterizedPreparedStatementSetter.class));
  }

}
