package org.cardanofoundation.explorer.rewards.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.account.model.AccountHistoryInner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStake;
import org.cardanofoundation.explorer.common.entity.ledgersync.EpochStakeCheckpoint;
import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHash;
import org.cardanofoundation.explorer.common.entity.ledgersync.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochStakeCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQEpochStakeCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.jooq.JOOQEpochStakeRepository;
import org.cardanofoundation.explorer.rewards.service.EpochService;

@ExtendWith(MockitoExtension.class)
class EpochStakeFetchingServiceImplTest {

  @Mock private StakeAddressRepository stakeAddressRepository;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private KoiosClient koiosClient;

  @Mock private PoolHashRepository poolHashRepository;

  @Mock private EpochStakeCheckpointRepository epochStakeCheckpointRepository;

  @Mock private EpochService epochService;

  @Mock private JOOQEpochStakeRepository jooqEpochStakeRepository;

  @Mock private JOOQEpochStakeCheckpointRepository jooqEpochStakeCheckpointRepository;

  @InjectMocks private EpochStakeFetchingServiceImpl epochStakeFetchingServiceImpl;

  @Captor ArgumentCaptor<List<EpochStake>> epochStakeCaptor;

  @Captor ArgumentCaptor<List<EpochStakeCheckpoint>> checkpointCaptor;

  private static List<String> stakeAddressList =
      new ArrayList<>(); //    verify(jdbcEpochStakeRepository).saveAll(anyList());

  @BeforeAll
  static void beforeAll() {
    stakeAddressList =
        List.of(
            "stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8",
            "stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg");
  }

  @Test
  void fetchData_shouldFetchAndSaveEpochStakeData() throws Exception {
    // Setup
    List<StakeAddress> stakeAddresses =
        List.of(
            StakeAddress.builder()
                .id(1L)
                .view("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
                .build(),
            StakeAddress.builder()
                .view("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
                .id(2L)
                .build());

    when(stakeAddressRepository.findByViewIn(any())).thenReturn(stakeAddresses);

    List<AccountHistory> accountHistoryList = new ArrayList<>();

    AccountHistory accountHistory1 = new AccountHistory();
    accountHistory1.setStakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8");

    AccountHistoryInner innerOfAccountHistory1 = new AccountHistoryInner();
    innerOfAccountHistory1.setEpochNo(414);
    innerOfAccountHistory1.setActiveStake("123456789");
    innerOfAccountHistory1.setPoolId("pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy");

    accountHistory1.setHistory(List.of(innerOfAccountHistory1));

    AccountHistory accountHistory2 = new AccountHistory();
    accountHistory2.setStakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg");

    AccountHistoryInner innerOfAccountHistory2 = new AccountHistoryInner();
    innerOfAccountHistory2.setEpochNo(414);
    innerOfAccountHistory2.setActiveStake("123456789");
    innerOfAccountHistory2.setPoolId("pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt");

    accountHistory2.setHistory(List.of(innerOfAccountHistory2));

    accountHistoryList.add(accountHistory1);
    accountHistoryList.add(accountHistory2);

    when(koiosClient.accountService().getAccountHistory(anyList(), any(), any()).getValue())
        .thenReturn(accountHistoryList);

    when(poolHashRepository.findByViewIn(any()))
        .thenReturn(
            List.of(
                PoolHash.builder()
                    .view("pool1pu5jlj4q9w9jlxeu370a3c9myx47md5j5m2str0naunn2q3lkdy")
                    .id(1L)
                    .build(),
                PoolHash.builder()
                    .view("pool1z5uqdk7dzdxaae5633fqfcu2eqzy3a3rgtuvy087fdld7yws0xt")
                    .id(2L)
                    .build()));

    when(epochStakeCheckpointRepository.findByStakeAddressIn(any()))
        .thenReturn(
            List.of(
                EpochStakeCheckpoint.builder()
                    .id(1L)
                    .stakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
                    .epochCheckpoint(413)
                    .build(),
                EpochStakeCheckpoint.builder()
                    .id(2L)
                    .stakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
                    .epochCheckpoint(413)
                    .build()));

    when(epochService.getCurrentEpoch()).thenReturn(414);
    // Run the test
    CompletableFuture<Boolean> result = epochStakeFetchingServiceImpl.fetchData(stakeAddressList);

    // Verify
    verify(jooqEpochStakeRepository).saveAll(epochStakeCaptor.capture());
    verify(jooqEpochStakeCheckpointRepository).saveAll(checkpointCaptor.capture());
    assertEquals(2, epochStakeCaptor.getValue().size());
    assertEquals(2, checkpointCaptor.getValue().size());
    assertTrue(result.get());
  }

  @Test
  void getStakeAddressListNeedFetchData_shouldReturnStakeAddressList() throws Exception {
    // Setup
    var checkpoints =
        List.of(
            EpochStakeCheckpoint.builder()
                .id(1L)
                .stakeAddress("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8")
                .epochCheckpoint(414)
                .build(),
            EpochStakeCheckpoint.builder()
                .id(2L)
                .stakeAddress("stake1u9nzg3s4wvstx0czh2asmeknfl80tn7z8nhm03smzunflas3m8ptg")
                .epochCheckpoint(415)
                .build());
    when(epochStakeCheckpointRepository.findByStakeAddressIn(any())).thenReturn(checkpoints);
    when(epochService.getCurrentEpoch()).thenReturn(415);

    // Run the test
    List<String> result =
        epochStakeFetchingServiceImpl.getStakeAddressListNeedFetchData(stakeAddressList);

    // Verify
    assertEquals(List.of("stake1u9kdeq0fzxqdgtdk73mxxpa88e29vffkggctzgul7dyqwmsvfm6z8"), result);
  }
}
