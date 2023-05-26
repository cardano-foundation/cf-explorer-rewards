package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.Reward3Repository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.Reward3FetchingService;
import org.jetbrains.annotations.NotNull;
import rest.koios.client.backend.api.account.model.AccountReward;
import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class Reward3FetchingServiceImpl implements Reward3FetchingService {
  private static final Object lock = new Object();

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;
  final Reward3Repository reward3Repository;
  final RewardCheckpointRepository rewardCheckpointRepository;
  final EpochRepository epochRepository;

  @Value("${application.reward.parallel-saving.enabled}")
  boolean rewardParallelSaving;
  @Value("${application.reward.parallel-saving.reward-sub-list-size}")
  int rewardSubListSize;
  @Value("${application.reward.parallel-saving.thread-num}")
  int savingRewardThreadNum;

  @Override
  @Async
  public CompletableFuture<List<AccountRewards>> fetchData(List<String> stakeAddressList)
      throws ApiException {
    var curTime = System.currentTimeMillis();

    Integer currentEpoch = epochRepository.findMaxEpoch();
    // we only fetch data with addresses that are not in the checkpoint table
    // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
    List<String> stakeAddressListNeedFetchData = getStakeAddressListNeedFetchData(stakeAddressList,
        currentEpoch);

    if (stakeAddressListNeedFetchData.isEmpty()) {
      log.info(
          "Reward: all stake addresses were in checkpoint and had epoch checkpoint = current epoch - 1");
      return CompletableFuture.completedFuture(new ArrayList<>());
    }

    List<AccountRewards> accountRewardsList = getAccountRewards(stakeAddressListNeedFetchData);

    int rewardSize = accountRewardsList
        .parallelStream()
        .mapToInt(accountRewards -> accountRewards.getRewards().size())
        .sum();

    log.info("fetch {} reward by koios api: {} ms, with stake_address input size {}",
        rewardSize, System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());

    return CompletableFuture.completedFuture(accountRewardsList);
  }

  @Override
  @Transactional(rollbackFor = {Exception.class})
  public void storeData(List<String> stakeAddressList, List<AccountRewards> accountRewardsList) {

    if (accountRewardsList.isEmpty()) {
      return;
    }

    synchronized (lock) {
      var curTime = System.currentTimeMillis();

      Integer currentEpoch = epochRepository.findMaxEpoch();

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountRewardsList);

      Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(
          stakeAddressList,
          accountRewardsList);

      List<Reward3> saveData = accountRewardsList.parallelStream()
          .flatMap(accountRewards -> {
            var rewardCheckpoint = rewardCheckpointMap.get(accountRewards.getStakeAddress());
            if (rewardCheckpoint == null) {
              return Stream.empty();
            }
            return accountRewards.getRewards().stream()
                .filter(accountReward -> accountReward.getEarnedEpoch()
                    > rewardCheckpoint.getEpochCheckpoint())
                .map(accountReward ->
                    Reward3.builder()
                        .pool(poolHashMap.get(accountReward.getPoolId()))
                        .addr(stakeAddressMap.get(accountRewards.getStakeAddress()))
                        .amount(new BigInteger(accountReward.getAmount()))
                        .earnedEpoch(accountReward.getEarnedEpoch())
                        .spendableEpoch(accountReward.getSpendableEpoch())
                        .type(RewardType.fromValue(accountReward.getType()))
                        .build()
                );
          })
          .collect(Collectors.toList());

      rewardCheckpointMap
          .values()
          .forEach(rewardCheckpoint -> rewardCheckpoint.setEpochCheckpoint(currentEpoch - 1));

      if (rewardParallelSaving) {
        saveRewardsConcurrently(saveData);
      } else {
        reward3Repository.saveAll(saveData);
      }

      rewardCheckpointRepository.saveAll(rewardCheckpointMap.values());

      log.info("Save {} reward record from koios api: {} ms, with stake_address input size {}",
          saveData.size(), System.currentTimeMillis() - curTime, stakeAddressList.size());
    }
  }

  @NotNull
  private Map<String, StakeAddress> getStakeAddressMap(List<String> stakeAddressList) {
    return stakeAddressRepository
        .findByViewIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(StakeAddress::getView, Function.identity()));
  }

  @NotNull
  private Map<String, PoolHash> getPoolHashMap(List<AccountRewards> accountRewardsList) {
    List<String> poolIds = accountRewardsList.stream()
        .flatMap(accountRewards -> accountRewards.getRewards().stream())
        .map(AccountReward::getPoolId)
        .toList();

    return poolHashRepository.findByViewIn(poolIds).stream()
        .collect(Collectors.toMap(PoolHash::getView, Function.identity()));
  }

  private List<AccountRewards> getAccountRewards(List<String> stakeAddressList)
      throws ApiException {
    return koiosClient
        .accountService()
        .getAccountRewards(stakeAddressList, null, null)
        .getValue();
  }

  private Map<String, RewardCheckpoint> getRewardCheckpointMap(List<String> stakeAddressList,
                                                               List<AccountRewards> accountRewardsList) {

    Map<String, RewardCheckpoint> rewardCheckpointMap = rewardCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(RewardCheckpoint::getStakeAddress, Function.identity()));

    List<RewardCheckpoint> rewardCheckpoints = accountRewardsList
        .stream()
        .filter(
            accountRewards -> !rewardCheckpointMap.containsKey(accountRewards.getStakeAddress()))
        .map(accountRewards -> RewardCheckpoint.builder()
            .stakeAddress(accountRewards.getStakeAddress())
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());

    rewardCheckpointMap.putAll(rewardCheckpoints.stream().collect(
        Collectors.toMap(RewardCheckpoint::getStakeAddress, Function.identity())));

    return rewardCheckpointMap;
  }

  private List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList,
                                                        int currentEpoch) {
    Map<String, RewardCheckpoint> rewardCheckpointMap = rewardCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(RewardCheckpoint::getStakeAddress, Function.identity()));

    return stakeAddressList.stream()
        .filter(stakeAddress -> (
            (!rewardCheckpointMap.containsKey(stakeAddress))
                || rewardCheckpointMap.get(stakeAddress).getEpochCheckpoint() < currentEpoch - 1
        ))
        .collect(Collectors.toList());
  }

  private void saveRewardsConcurrently(List<Reward3> rewards) {
    ExecutorService executorService = Executors.newFixedThreadPool(savingRewardThreadNum);

    try {
      List<CompletableFuture<Void>> saveFutures = new ArrayList<>();


      List<List<Reward3>> batches = new ArrayList<>();
      for (int i = 0; i < rewards.size(); i += rewardSubListSize) {
        int endIndex = Math.min(i + rewardSubListSize, rewards.size());
        List<Reward3> batch = rewards.subList(i, endIndex);
        batches.add(batch);
      }

      for (List<Reward3> batch : batches) {
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
          reward3Repository.saveAll(batch);
        }, executorService);

        saveFutures.add(saveFuture);
      }

      CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0])).join();
    } finally {
      executorService.shutdown();

      try {
        if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
          executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
        executorService.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}
