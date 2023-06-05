package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.math.BigInteger;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.test.context.ActiveProfiles;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
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
class JDBCEpochStakeRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCEpochStakeRepository jdbcEpochStakeRepository;

  @BeforeEach
  void setUp() {
    jdbcEpochStakeRepository = new JDBCEpochStakeRepository(jdbcTemplate, 100);
  }

  @Test
  void testSaveAll() {
    final var epochStake1 = EpochStake.builder()
        .epochNo(414)
        .pool(PoolHash.builder().id(1l).build())
        .addr(StakeAddress.builder().id(1l).build())
        .amount(new BigInteger("100000000"))
        .build();
    final var epochStake2 = EpochStake.builder()
        .epochNo(414)
        .pool(PoolHash.builder().id(2l).build())
        .addr(StakeAddress.builder().id(2l).build())
        .amount(new BigInteger("100000000"))
        .build();

    final List<EpochStake> epochStakeList = List.of(epochStake1, epochStake2);

    jdbcEpochStakeRepository.saveAll(epochStakeList);

    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO epoch_stake (id, epoch_no, amount, addr_id, pool_id) "
            + "VALUES (nextval('epoch_stake_id_seq'), ?, ?, ?, ?) "
            + "ON CONFLICT (addr_id, epoch_no, pool_id) DO NOTHING"),
        eq(epochStakeList), eq(100), any(ParameterizedPreparedStatementSetter.class));
  }
}
