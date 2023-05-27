package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.cardanofoundation.explorer.consumercommon.entity.Delegation;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rest.koios.client.backend.api.account.model.AccountReward;
import rest.koios.client.backend.api.account.model.AccountRewards;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class RewardFetchingServiceImplTest {

  @Mock
  private KoiosClient mockKoiosClient;
  @Mock
  private StakeAddressRepository mockStakeAddressRepository;
  @Mock
  private PoolHashRepository mockPoolHashRepository;
  @Mock
  private RewardRepository mockRewardRepository;
  @Mock
  private RewardCheckpointRepository mockRewardCheckpointRepository;
  @Mock
  private EpochRepository mockEpochRepository;

  @InjectMocks
  private RewardFetchingServiceImpl rewardFetchingService;

  @Test
  void testFetchData() throws Exception {
    when(mockEpochRepository.findMaxEpoch()).thenReturn(413);

    List<String> stakeAddressList = Arrays.asList(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6uky"
            + "v5267j70fzxgw0fr3z8zeac5vyqhf9jhy");

    final List<RewardCheckpoint> rewardCheckpoints = List.of(
        new RewardCheckpoint("stakeAddress", 0));

    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(rewardCheckpoints);

    when(mockKoiosClient.accountService()).thenReturn(null);

    final CompletableFuture<List<AccountRewards>> result = rewardFetchingService.fetchData(
        stakeAddressList);

    // Verify the results
  }

  @Test
  void testFetchData_RewardCheckpointRepositoryReturnsNoItems() throws Exception {
    // Setup
    when(mockEpochRepository.findMaxEpoch()).thenReturn(0);
    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(Collections.emptyList());
    when(mockKoiosClient.accountService()).thenReturn(null);

    // Run the test
    final CompletableFuture<List<AccountRewards>> result = rewardFetchingService.fetchData(
        List.of("value"));

    // Verify the results
  }

  @Test
  void testStoreData() {
    // Setup
    final AccountRewards accountRewards = new AccountRewards();
    accountRewards.setStakeAddress("stakeAddress");
    final AccountReward accountReward = new AccountReward();
    accountReward.setEarnedEpoch(0);
    accountReward.setSpendableEpoch(0);
    accountReward.setAmount("amount");
    accountReward.setType("type");
    accountReward.setPoolId("poolId");
    accountRewards.setRewards(List.of(accountReward));
    final List<AccountRewards> accountRewardsList = List.of(accountRewards);
    when(mockEpochRepository.findMaxEpoch()).thenReturn(0);

    // Configure StakeAddressRepository.findByViewIn(...).
    final StakeAddress stakeAddress = new StakeAddress();
    stakeAddress.setHashRaw("hashRaw");
    stakeAddress.setView("view");
    stakeAddress.setScriptHash("scriptHash");
    stakeAddress.setBalance(new BigInteger("100"));
    stakeAddress.setAvailableReward(new BigInteger("100"));
    final List<StakeAddress> stakeAddresses = List.of(stakeAddress);
    when(mockStakeAddressRepository.findByViewIn(List.of("value"))).thenReturn(stakeAddresses);

    // Configure PoolHashRepository.findByViewIn(...).
    final PoolHash poolHash = new PoolHash();
    poolHash.setHashRaw("hashRaw");
    poolHash.setView("view");
    poolHash.setPoolSize(new BigInteger("100"));
    final Delegation delegation = new Delegation();
    final StakeAddress address = new StakeAddress();
    delegation.setAddress(address);
    poolHash.setDelegations(List.of(delegation));
    final List<PoolHash> poolHashes = List.of(poolHash);
    when(mockPoolHashRepository.findByViewIn(List.of("value"))).thenReturn(poolHashes);

    // Configure RewardCheckpointRepository.findByStakeAddressIn(...).
    final List<RewardCheckpoint> rewardCheckpoints = List.of(
        new RewardCheckpoint("stakeAddress", 0));
    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(rewardCheckpoints);

    // Run the test
    rewardFetchingService.storeData(List.of("value"), accountRewardsList);

    // Verify the results
    // Confirm RewardRepository.saveAll(...).
    final Reward reward = new Reward();
    final StakeAddress addr = new StakeAddress();
    addr.setHashRaw("hashRaw");
    addr.setView("view");
    addr.setScriptHash("scriptHash");
    addr.setBalance(new BigInteger("100"));
    reward.setAddr(addr);
    final List<Reward> entities = List.of(reward);
    verify(mockRewardRepository).saveAll(entities);
    verify(mockRewardCheckpointRepository).saveAll(
        List.of(new RewardCheckpoint("stakeAddress", 0)));
  }

  @Test
  void testStoreData_StakeAddressRepositoryReturnsNoItems() {
    // Setup
    final AccountRewards accountRewards = new AccountRewards();
    accountRewards.setStakeAddress("stakeAddress");
    final AccountReward accountReward = new AccountReward();
    accountReward.setEarnedEpoch(0);
    accountReward.setSpendableEpoch(0);
    accountReward.setAmount("amount");
    accountReward.setType("type");
    accountReward.setPoolId("poolId");
    accountRewards.setRewards(List.of(accountReward));
    final List<AccountRewards> accountRewardsList = List.of(accountRewards);
    when(mockEpochRepository.findMaxEpoch()).thenReturn(0);
    when(mockStakeAddressRepository.findByViewIn(List.of("value")))
        .thenReturn(Collections.emptyList());

    // Configure PoolHashRepository.findByViewIn(...).
    final PoolHash poolHash = new PoolHash();
    poolHash.setHashRaw("hashRaw");
    poolHash.setView("view");
    poolHash.setPoolSize(new BigInteger("100"));
    final Delegation delegation = new Delegation();
    final StakeAddress address = new StakeAddress();
    delegation.setAddress(address);
    poolHash.setDelegations(List.of(delegation));
    final List<PoolHash> poolHashes = List.of(poolHash);
    when(mockPoolHashRepository.findByViewIn(List.of("value"))).thenReturn(poolHashes);

    // Configure RewardCheckpointRepository.findByStakeAddressIn(...).
    final List<RewardCheckpoint> rewardCheckpoints = List.of(
        new RewardCheckpoint("stakeAddress", 0));
    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(rewardCheckpoints);

    // Run the test
    rewardFetchingService.storeData(List.of("value"), accountRewardsList);

    // Verify the results
    // Confirm RewardRepository.saveAll(...).
    final Reward reward = new Reward();
    final StakeAddress addr = new StakeAddress();
    addr.setHashRaw("hashRaw");
    addr.setView("view");
    addr.setScriptHash("scriptHash");
    addr.setBalance(new BigInteger("100"));
    reward.setAddr(addr);
    final List<Reward> entities = List.of(reward);
    verify(mockRewardRepository).saveAll(entities);
    verify(mockRewardCheckpointRepository).saveAll(
        List.of(new RewardCheckpoint("stakeAddress", 0)));
  }

  @Test
  void testStoreData_PoolHashRepositoryReturnsNoItems() {
    // Setup
    final AccountRewards accountRewards = new AccountRewards();
    accountRewards.setStakeAddress("stakeAddress");
    final AccountReward accountReward = new AccountReward();
    accountReward.setEarnedEpoch(0);
    accountReward.setSpendableEpoch(0);
    accountReward.setAmount("amount");
    accountReward.setType("type");
    accountReward.setPoolId("poolId");
    accountRewards.setRewards(List.of(accountReward));
    final List<AccountRewards> accountRewardsList = List.of(accountRewards);
    when(mockEpochRepository.findMaxEpoch()).thenReturn(0);

    // Configure StakeAddressRepository.findByViewIn(...).
    final StakeAddress stakeAddress = new StakeAddress();
    stakeAddress.setHashRaw("hashRaw");
    stakeAddress.setView("view");
    stakeAddress.setScriptHash("scriptHash");
    stakeAddress.setBalance(new BigInteger("100"));
    stakeAddress.setAvailableReward(new BigInteger("100"));
    final List<StakeAddress> stakeAddresses = List.of(stakeAddress);
    when(mockStakeAddressRepository.findByViewIn(List.of("value"))).thenReturn(stakeAddresses);

    when(mockPoolHashRepository.findByViewIn(List.of("value"))).thenReturn(Collections.emptyList());

    // Configure RewardCheckpointRepository.findByStakeAddressIn(...).
    final List<RewardCheckpoint> rewardCheckpoints = List.of(
        new RewardCheckpoint("stakeAddress", 0));
    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(rewardCheckpoints);

    // Run the test
    rewardFetchingService.storeData(List.of("value"), accountRewardsList);

    // Verify the results
    // Confirm RewardRepository.saveAll(...).
    final Reward reward = new Reward();
    final StakeAddress addr = new StakeAddress();
    addr.setHashRaw("hashRaw");
    addr.setView("view");
    addr.setScriptHash("scriptHash");
    addr.setBalance(new BigInteger("100"));
    reward.setAddr(addr);
    final List<Reward> entities = List.of(reward);
    verify(mockRewardRepository).saveAll(entities);
    verify(mockRewardCheckpointRepository).saveAll(
        List.of(new RewardCheckpoint("stakeAddress", 0)));
  }

  @Test
  void testStoreData_RewardCheckpointRepositoryFindByStakeAddressInReturnsNoItems() {
    // Setup
    final AccountRewards accountRewards = new AccountRewards();
    accountRewards.setStakeAddress("stakeAddress");
    final AccountReward accountReward = new AccountReward();
    accountReward.setEarnedEpoch(0);
    accountReward.setSpendableEpoch(0);
    accountReward.setAmount("amount");
    accountReward.setType("type");
    accountReward.setPoolId("poolId");
    accountRewards.setRewards(List.of(accountReward));
    final List<AccountRewards> accountRewardsList = List.of(accountRewards);
    when(mockEpochRepository.findMaxEpoch()).thenReturn(0);

    // Configure StakeAddressRepository.findByViewIn(...).
    final StakeAddress stakeAddress = new StakeAddress();
    stakeAddress.setHashRaw("hashRaw");
    stakeAddress.setView("view");
    stakeAddress.setScriptHash("scriptHash");
    stakeAddress.setBalance(new BigInteger("100"));
    stakeAddress.setAvailableReward(new BigInteger("100"));
    final List<StakeAddress> stakeAddresses = List.of(stakeAddress);
    when(mockStakeAddressRepository.findByViewIn(List.of("value"))).thenReturn(stakeAddresses);

    // Configure PoolHashRepository.findByViewIn(...).
    final PoolHash poolHash = new PoolHash();
    poolHash.setHashRaw("hashRaw");
    poolHash.setView("view");
    poolHash.setPoolSize(new BigInteger("100"));
    final Delegation delegation = new Delegation();
    final StakeAddress address = new StakeAddress();
    delegation.setAddress(address);
    poolHash.setDelegations(List.of(delegation));
    final List<PoolHash> poolHashes = List.of(poolHash);
    when(mockPoolHashRepository.findByViewIn(List.of("value"))).thenReturn(poolHashes);

    when(mockRewardCheckpointRepository.findByStakeAddressIn(List.of("value")))
        .thenReturn(Collections.emptyList());

    // Run the test
    rewardFetchingService.storeData(List.of("value"), accountRewardsList);

    // Verify the results
    // Confirm RewardRepository.saveAll(...).
    final Reward reward = new Reward();
    final StakeAddress addr = new StakeAddress();
    addr.setHashRaw("hashRaw");
    addr.setView("view");
    addr.setScriptHash("scriptHash");
    addr.setBalance(new BigInteger("100"));
    reward.setAddr(addr);
    final List<Reward> entities = List.of(reward);
    verify(mockRewardRepository).saveAll(entities);
    verify(mockRewardCheckpointRepository).saveAll(
        List.of(new RewardCheckpoint("stakeAddress", 0)));
  }
}
