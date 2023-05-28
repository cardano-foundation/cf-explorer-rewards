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
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.RewardFetchingService;
import org.jetbrains.annotations.NotNull;
import rest.koios.client.backend.api.account.model.AccountReward;
import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class RewardFetchingServiceImpl implements RewardFetchingService {

  private static final Object lock1 = new Object();
  private static final Object lock2 = new Object();

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;
  final RewardRepository rewardRepository;
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
      synchronized (lock1) {
        Integer currentEpoch = epochRepository.findMaxEpoch();

        Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(stakeAddressList);

        rewardCheckpointMap
            .values()
            .forEach(rewardCheckpoint -> rewardCheckpoint.setEpochCheckpoint(currentEpoch - 1));

        rewardCheckpointRepository.saveAll(rewardCheckpointMap.values());
      }
      return;
    }

    synchronized (lock2) {
      var curTime = System.currentTimeMillis();

      Integer currentEpoch = epochRepository.findMaxEpoch();

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountRewardsList);

      Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(stakeAddressList);

      List<Reward> saveData = accountRewardsList.parallelStream()
          .flatMap(accountRewards -> {
            var rewardCheckpoint = rewardCheckpointMap.get(accountRewards.getStakeAddress());
            if (rewardCheckpoint == null) {
              return Stream.empty();
            }
            return accountRewards.getRewards().stream()
                .filter(accountReward -> accountReward.getEarnedEpoch()
                    > rewardCheckpoint.getEpochCheckpoint())
                .map(accountReward ->
                    Reward.builder()
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
        rewardRepository.saveAll(saveData);
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

  /**
   * fetch data using koios java client
   *
   * @param stakeAddressList
   * @return
   * @throws ApiException
   */
  private List<AccountRewards> getAccountRewards(List<String> stakeAddressList)
      throws ApiException {
    return koiosClient
        .accountService()
        .getAccountRewards(stakeAddressList, null, null)
        .getValue();
  }

  private Map<String, RewardCheckpoint> getRewardCheckpointMap(List<String> stakeAddressList) {
    // get reward checkpoint map with stakeAddressList
    Map<String, RewardCheckpoint> rewardCheckpointMap = rewardCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(RewardCheckpoint::getStakeAddress, Function.identity()));

    // if an stake address not in checkpoint table,
    // create a rewardCheckpoint with stake address equal to that and epoch_checkpoint = 0
    List<RewardCheckpoint> rewardCheckpoints = stakeAddressList
        .stream()
        .filter(
            stakeAddress -> !rewardCheckpointMap.containsKey(stakeAddress))
        .map(stakeAddress -> RewardCheckpoint.builder()
            .stakeAddress(stakeAddress)
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());
    // put all into result
    rewardCheckpointMap.putAll(rewardCheckpoints.stream().collect(
        Collectors.toMap(RewardCheckpoint::getStakeAddress, Function.identity())));

    return rewardCheckpointMap;
  }

  /**
   * get stake address list that are not in the checkpoint table or in the checkpoint table but have
   * an epoch checkpoint value < (current epoch - 1)
   *
   * @param stakeAddressList
   * @param currentEpoch
   * @return
   */
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

  /**
   * Divide the reward list into parts and save them to the database concurrently
   *
   * @param rewards
   */
  private void saveRewardsConcurrently(List<Reward> rewards) {
    ExecutorService executorService = Executors.newFixedThreadPool(savingRewardThreadNum);

    try {
      List<CompletableFuture<Void>> saveFutures = new ArrayList<>();

      List<List<Reward>> batches = new ArrayList<>();
      for (int i = 0; i < rewards.size(); i += rewardSubListSize) {
        int endIndex = Math.min(i + rewardSubListSize, rewards.size());
        List<Reward> batch = rewards.subList(i, endIndex);
        batches.add(batch);
      }

      for (List<Reward> batch : batches) {
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
          rewardRepository.saveAll(batch);
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
