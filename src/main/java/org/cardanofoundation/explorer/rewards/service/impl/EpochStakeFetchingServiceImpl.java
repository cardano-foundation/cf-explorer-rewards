package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import org.cardanofoundation.explorer.consumercommon.entity.EpochStake;
import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.EpochStakeRepository;
import org.cardanofoundation.explorer.rewards.repository.EpochStakeCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.EpochStakeFetchingService;
import org.jetbrains.annotations.NotNull;
import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.account.model.AccountHistoryInner;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class EpochStakeFetchingServiceImpl implements EpochStakeFetchingService {

  private static final Object lock1 = new Object();
  private static final Object lock2 = new Object();

  final StakeAddressRepository stakeAddressRepository;
  final KoiosClient koiosClient;
  final PoolHashRepository poolHashRepository;
  final EpochStakeRepository epochStakeRepository;
  final EpochStakeCheckpointRepository epochStakeCheckpointRepository;
  final EpochRepository epochRepository;

  @Value("${application.epoch-stake.parallel-saving.enabled}")
  boolean epochStakeParallelSaving;
  @Value("${application.epoch-stake.parallel-saving.epoch-stake-sub-list-size}")
  int epochStakeSubListSize;
  @Value("${application.epoch-stake.parallel-saving.thread-num}")
  int savingEpochStakeThreadNum;

  @Override
  @Async
  public CompletableFuture<List<AccountHistory>> fetchData(List<String> stakeAddressList)
      throws ApiException {

    var curTime = System.currentTimeMillis();
    Integer currentEpoch = epochRepository.findMaxEpoch();

    // we only fetch data with addresses that are not in the checkpoint table
    // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
    List<String> stakeAddressListNeedFetchData = getStakeAddressListNeedFetchData(stakeAddressList,
        currentEpoch);

    if (stakeAddressListNeedFetchData.isEmpty()) {
      log.info(
          "EpochStake: all stake addresses were in checkpoint and had epoch checkpoint = current epoch - 1");
      return CompletableFuture.completedFuture(new ArrayList<>());
    }

    List<AccountHistory> accountHistoryList = getAccountHistoryList(stakeAddressListNeedFetchData);

    int epochStakeSize = accountHistoryList
        .parallelStream()
        .mapToInt(accountHistory -> accountHistory.getHistory().size())
        .sum();

    log.info("fetch {} epoch_stake by koios api: {} ms, with stake_address input size {}",
        epochStakeSize, System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());

    return CompletableFuture.completedFuture(accountHistoryList);
  }

  @Override
  @Transactional(rollbackFor = {Exception.class})
  public void storeData(List<String> stakeAddressList, List<AccountHistory> accountHistoryList) {
    if (accountHistoryList.isEmpty()) {
      synchronized (lock1) {
        Integer currentEpoch = epochRepository.findMaxEpoch();
        Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap = getEpochStakeCheckpointMap(
            stakeAddressList);
        epochStakeCheckpointMap
            .values()
            .forEach(epochCheckpoint -> epochCheckpoint.setEpochCheckpoint(currentEpoch - 1));
        epochStakeCheckpointRepository.saveAll(epochStakeCheckpointMap.values());
      }
      return;
    }
    synchronized (lock2) {
      var curTime = System.currentTimeMillis();

      Integer currentEpoch = epochRepository.findMaxEpoch();

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountHistoryList);

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

      Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap = getEpochStakeCheckpointMap(
          stakeAddressList);

      List<EpochStake> saveData = accountHistoryList.parallelStream()
          .flatMap(accountHistory -> {
            var epochStakeCheckpoint = epochStakeCheckpointMap.get(
                accountHistory.getStakeAddress());
            if (epochStakeCheckpoint == null) {
              return Stream.empty();
            }

            return accountHistory.getHistory().stream()
                .filter(accountHistoryInner ->
                    accountHistoryInner.getEpochNo() > epochStakeCheckpoint.getEpochCheckpoint()
                        && !Objects.equals(accountHistoryInner.getEpochNo(), currentEpoch))
                .map(accountHistoryInner ->
                    EpochStake.builder()
                        .epochNo(accountHistoryInner.getEpochNo())
                        .addr(stakeAddressMap.get(accountHistory.getStakeAddress()))
                        .pool(poolHashMap.get(accountHistoryInner.getPoolId()))
                        .amount(new BigInteger(accountHistoryInner.getActiveStake())).build()
                );
          })
          .collect(Collectors.toList());

      epochStakeCheckpointMap
          .values()
          .forEach(epochCheckpoint -> epochCheckpoint.setEpochCheckpoint(currentEpoch - 1));
      if (epochStakeParallelSaving) {
        saveEpochStakesConcurrently(saveData);
      } else {
        epochStakeRepository.saveAll(saveData);
      }
      epochStakeCheckpointRepository.saveAll(epochStakeCheckpointMap.values());

      log.info("Save {} epoch_stake complete in: {} ms, with stake_address input size {}",
          saveData.size(), System.currentTimeMillis() - curTime, stakeAddressList.size());
    }
  }


  private Map<String, EpochStakeCheckpoint> getEpochStakeCheckpointMap(
      List<String> stakeAddressList) {
    // get epoch stake checkpoint map with stakeAddressList
    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap = epochStakeCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity()));

    // if an stake address not in checkpoint table,
    // create a epochStakeCheckpoint with stake address equal to that and epoch_checkpoint = 0
    List<EpochStakeCheckpoint> epochStakeCheckpoints = stakeAddressList
        .stream()
        .filter(
            stakeAddress -> !epochStakeCheckpointMap.containsKey(stakeAddress))
        .map(stakeAddress -> EpochStakeCheckpoint.builder()
            .stakeAddress(stakeAddress)
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());
    // put all into result
    epochStakeCheckpointMap.putAll(epochStakeCheckpoints.stream().collect(
        Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity())));

    return epochStakeCheckpointMap;
  }

  @NotNull
  private Map<String, StakeAddress> getStakeAddressMap(List<String> stakeAddressList) {
    return stakeAddressRepository
        .findByViewIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(StakeAddress::getView, Function.identity()));
  }

  @NotNull
  private Map<String, PoolHash> getPoolHashMap(List<AccountHistory> accountHistoryList) {
    List<String> poolIds = accountHistoryList.stream()
        .flatMap(accountHistory -> accountHistory.getHistory().stream())
        .map(AccountHistoryInner::getPoolId)
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
  private List<AccountHistory> getAccountHistoryList(List<String> stakeAddressList)
      throws ApiException {
    return koiosClient.accountService()
        .getAccountHistory(stakeAddressList, null, null)
        .getValue();
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
    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap = epochStakeCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity()));

    return stakeAddressList.stream()
        .filter(stakeAddress ->
            ((!epochStakeCheckpointMap.containsKey(stakeAddress))
                || epochStakeCheckpointMap.get(stakeAddress).getEpochCheckpoint() < currentEpoch - 1
            ))
        .collect(Collectors.toList());
  }

  /**
   * Divide the epochStake list into parts and save them to the database concurrently
   *
   * @param epochStakeList
   */
  private void saveEpochStakesConcurrently(List<EpochStake> epochStakeList) {
    ExecutorService executorService = Executors.newFixedThreadPool(savingEpochStakeThreadNum);

    try {
      List<CompletableFuture<Void>> saveFutures = new ArrayList<>();

      List<List<EpochStake>> batches = new ArrayList<>();
      for (int i = 0; i < epochStakeList.size(); i += epochStakeSubListSize) {
        int endIndex = Math.min(i + epochStakeSubListSize, epochStakeList.size());
        List<EpochStake> batch = epochStakeList.subList(i, endIndex);
        batches.add(batch);
      }

      for (List<EpochStake> batch : batches) {
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
          epochStakeRepository.saveAll(batch);
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
