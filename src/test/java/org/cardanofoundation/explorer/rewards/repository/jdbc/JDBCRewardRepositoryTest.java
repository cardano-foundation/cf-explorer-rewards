package org.cardanofoundation.explorer.rewards.repository.jdbc;

import java.math.BigInteger;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.test.context.ActiveProfiles;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JDBCRewardRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  private JDBCRewardRepository jdbcRewardRepository;

  private final int batchSize = 100;

  @BeforeEach
  void setUp() {
    jdbcRewardRepository = new JDBCRewardRepository(jdbcTemplate, batchSize);
  }

  @Test
  void testSaveAll() {
    final var reward1 = Reward.builder()
        .addr(StakeAddress.builder().id(1L).build())
        .pool(PoolHash.builder().id(1l).build())
        .type(RewardType.MEMBER)
        .spendableEpoch(415)
        .earnedEpoch(414)
        .amount(new BigInteger("10000000"))
        .build();

    final var reward2 = Reward.builder()
        .addr(StakeAddress.builder().id(2L).build())
        .pool(PoolHash.builder().id(2l).build())
        .type(RewardType.MEMBER)
        .spendableEpoch(415)
        .earnedEpoch(414)
        .amount(new BigInteger("10000000"))
        .build();

    final List<Reward> rewards = List.of(reward1, reward2);

    jdbcRewardRepository.saveAll(rewards);

    verify(jdbcTemplate).batchUpdate(
        eq("INSERT INTO reward (id, type, amount, earned_epoch, spendable_epoch, addr_id, pool_id)"
            + " VALUES (nextval('reward_id_seq'), ?, ?, ?, ?, ?, ?) ON CONFLICT (addr_id, type, earned_epoch, pool_id) DO NOTHING"),
        eq(rewards), eq(batchSize), any(ParameterizedPreparedStatementSetter.class));
  }

}
