package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import org.cardanofoundation.explorer.consumercommon.entity.PoolInfo;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JDBCPoolInfoRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCPoolInfoRepository jdbcPoolInfoRepository;

  private final int batchSize = 100;

  @BeforeEach
  void setUp() {
    jdbcPoolInfoRepository = new JDBCPoolInfoRepository(jdbcTemplate, batchSize);
  }

  @Test
  void testSaveAll() {
    final var poolInfo1 = PoolInfo.builder()
        .poolId("pool1030as3pp5684ghgf4kzcpv4p2jnmkmme7j363t95690zwxp7wa0")
        .activeStake("1385435033958")
        .fetchedAtEpoch(415)
        .liveStake("20000000000")
        .liveSaturation(0.03)
        .build();

    final var poolInfo2 = PoolInfo.builder()
        .poolId("pool10l55ku75rrntva736tk2fxu4fkkym9s98gsujlfek8pw54x28tt")
        .activeStake("1385438883958")
        .fetchedAtEpoch(415)
        .liveStake("10000000000")
        .liveSaturation(1.50)
        .build();

    final List<PoolInfo> poolInfoList = List.of(poolInfo1, poolInfo2);

    jdbcPoolInfoRepository.saveAll(poolInfoList);

    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO pool_info (id, pool_id, fetched_at_epoch, active_stake, live_stake, live_saturation) "
            + "VALUES (nextval('pool_info_id_seq'),?, ?, ?, ?, ?)  ON CONFLICT (pool_id, fetched_at_epoch) "
            + "DO NOTHING"),
        eq(poolInfoList), eq(batchSize),
        any(ParameterizedPreparedStatementSetter.class));
  }
}
