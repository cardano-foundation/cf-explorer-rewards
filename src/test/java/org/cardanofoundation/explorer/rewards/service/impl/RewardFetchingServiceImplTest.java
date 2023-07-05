package org.cardanofoundation.explorer.rewards.service.impl;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQRewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQRewardRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.rewards.service.EpochService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.account.model.AccountReward;
import rest.koios.client.backend.api.account.model.AccountRewards;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardFetchingServiceImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock
  private StakeAddressRepository stakeAddressRepository;

  @Mock
  private PoolHashRepository poolHashRepository;

  @Mock
  private RewardCheckpointRepository rewardCheckpointRepository;

  @Mock
  private EpochService epochService;

  @Mock
  private JOOQRewardRepository jooqRewardRepository;

  @Mock
  private JOOQRewardCheckpointRepository jooqRewardCheckpointRepository;

  @InjectMocks
  private RewardFetchingServiceImpl rewardFetchingServiceImpl;

  @Captor
  ArgumentCaptor<List<Reward>> rewardCaptor;

  @Captor
  ArgumentCaptor<List<RewardCheckpoint>> checkpointCaptor;

  private static List<String> stakeAddressList = new ArrayList<>();

  @BeforeAll
  static void beforeAll() {
    stakeAddressList = List.of("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8",
        "stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg");
  }

  @Test
  void fetchData_shouldFetchAndSaveRewardData() throws Exception {
    // Setup
    List<StakeAddress> stakeAddresses = List.of(
        StakeAddress.builder()
            .id(1L)
            .view("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
            .build(),
        StakeAddress.builder()
            .view("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
            .id(2L)
            .build());

    when(stakeAddressRepository.findByViewIn(any())).thenReturn(stakeAddresses);

    List<AccountRewards> accountRewards = new ArrayList<>();

    AccountRewards accountRewards1 = new AccountRewards();
    accountRewards1.setStakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8");

    AccountReward inner1 = new AccountReward();
    inner1.setAmount("99999999");
    inner1.setPoolId("pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");
    inner1.setType("member");
    inner1.setEarnedEpoch(414);
    inner1.setSpendableEpoch(415);

    accountRewards1.setRewards(List.of(
      inner1
    ));

    AccountRewards accountRewards2 = new AccountRewards();
    accountRewards2.setStakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg");

    AccountReward inner2 = new AccountReward();
    inner2.setAmount("99999999");
    inner2.setPoolId("pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt");
    inner2.setType("member");
    inner2.setEarnedEpoch(414);
    inner2.setSpendableEpoch(415);

    accountRewards2.setRewards(List.of(
        inner2
    ));

    accountRewards.add(accountRewards1);
    accountRewards.add(accountRewards2);

    when(koiosClient.accountService()
        .getAccountRewards(
            anyList(), any(), any())
        .getValue()).thenReturn(accountRewards);

    when(poolHashRepository.findByViewIn(any())).thenReturn(List.of(
        PoolHash.builder().view("pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy").id(1L)
            .build(),
        PoolHash.builder().view("pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt").id(2L)
            .build()));

    when(rewardCheckpointRepository.findByStakeAddressIn(any())).thenReturn(
        List.of(RewardCheckpoint.builder().id(1L)
                .stakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
                .epochCheckpoint(413).build(),
            RewardCheckpoint.builder().id(2L)
                .stakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
                .epochCheckpoint(413).build()));

    when(epochService.getCurrentEpoch()).thenReturn(416);

    // Run the test
    CompletableFuture<Boolean> result = rewardFetchingServiceImpl.fetchData(stakeAddressList);

    // Verify
    verify(jooqRewardRepository).saveAll(rewardCaptor.capture());
    verify(jooqRewardCheckpointRepository).saveAll(checkpointCaptor.capture());
    assertEquals(2, rewardCaptor.getValue().size());
    assertEquals(2, checkpointCaptor.getValue().size());
    assertEquals(Boolean.TRUE, result.get());
  }

  @Test
  void getStakeAddressListNeedFetchData_shouldReturnStakeAddressList() throws Exception {
    // Setup
    var checkpoints = List.of(RewardCheckpoint.builder().id(1L)
            .stakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
            .epochCheckpoint(413).build(),
        RewardCheckpoint.builder().id(2L)
            .stakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
            .epochCheckpoint(414).build());
    when(rewardCheckpointRepository.findByStakeAddressIn(any())).thenReturn(checkpoints);
    when(epochService.getCurrentEpoch()).thenReturn(415);

    // Run the test
    List<String> result = rewardFetchingServiceImpl.getStakeAddressListNeedFetchData(
        stakeAddressList);
    // Verify
    assertEquals(List.of("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8"), result);
  }
}
