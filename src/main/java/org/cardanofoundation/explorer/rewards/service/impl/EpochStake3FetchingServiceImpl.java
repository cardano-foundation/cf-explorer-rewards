package org.cardanofoundation.explorer.rewards.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.explorer.consumercommon.entity.EpochStakeCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
import org.cardanofoundation.explorer.rewards.repository.*;
import org.cardanofoundation.explorer.rewards.service.EpochStake3FetchingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.account.model.AccountHistoryInner;
import rest.koios.client.backend.api.base.exception.ApiException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class EpochStake3FetchingServiceImpl implements EpochStake3FetchingService {

  final StakeAddressRepository stakeAddressRepository;
  final KoiosClient koiosClient;
  final PoolHashRepository poolHashRepository;
  final EpochStake3Repository epochStakeRepository;
  final EpochStakeCheckpointRepository epochStakeCheckpointRepository;
  final EpochRepository epochRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(List<String> stakeAddressList) {
    List<EpochStake3> result = new ArrayList<>();
    try {
      var curTime = System.currentTimeMillis();
      Integer currentEpoch = epochRepository.findMaxEpoch();

      // we only fetch data with addresses that are not in the checkpoint table
      // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
      List<String> stakeAddressListNeedFetchData = getStakeAddressListNeedFetchData(stakeAddressList, currentEpoch);

      if (stakeAddressListNeedFetchData.isEmpty()) {
          log.info("EpochStake: all stake addresses were in checkpoint and had epoch checkpoint = current epoch - 1");
          return CompletableFuture.completedFuture(true);
      }

      List<AccountHistory> accountHistoryList = getAccountHistoryList(stakeAddressListNeedFetchData);

      int epochStakeSize = accountHistoryList
          .parallelStream()
          .mapToInt(accountHistory -> accountHistory.getHistory().size())
          .sum();

      log.info("fetch {} epoch_stake by koios api: {} ms, with stake_address input size {}",
          epochStakeSize, System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountHistoryList);

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressListNeedFetchData);

      Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap =
          getEpochStakeCheckpointMap(stakeAddressListNeedFetchData, accountHistoryList);

      for (var accountHistory : accountHistoryList) {
        var epochStakeCheckpoint = epochStakeCheckpointMap.get(accountHistory.getStakeAddress());

        if (epochStakeCheckpoint == null) {
          continue;
        }
        for (AccountHistoryInner accountHistoryInner : accountHistory.getHistory()) {
          if (accountHistoryInner.getEpochNo() <= epochStakeCheckpoint.getEpochCheckpoint()
              || Objects.equals(accountHistoryInner.getEpochNo(), currentEpoch)) {
            continue;
          }
          var item = EpochStake3.builder()
              .epochNo(accountHistoryInner.getEpochNo())
              .addr(stakeAddressMap.get(accountHistory.getStakeAddress()))
              .pool(poolHashMap.get(accountHistoryInner.getPoolId()))
              .amount(new BigInteger(accountHistoryInner.getActiveStake())).build();

          result.add(item);
        }
      }

      epochStakeCheckpointMap
          .values()
          .forEach(epochCheckpoint -> epochCheckpoint.setEpochCheckpoint(currentEpoch - 1));
      epochStakeRepository.saveAll(result);
      epochStakeCheckpointRepository.saveAll(epochStakeCheckpointMap.values());

      log.info("fetch and save {} epoch_stake complete in: {} ms, with stake_address input size {}",
          result.size(), System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());
    } catch (ApiException e) {
      log.error("Exception when fetching epoch stake data", e);
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    return CompletableFuture.completedFuture(Boolean.TRUE);
  }


  private Map<String, EpochStakeCheckpoint> getEpochStakeCheckpointMap(
      List<String> stakeAddressList,
      List<AccountHistory> accountHistoryList) {

    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap = epochStakeCheckpointRepository
        .findByStakeAddressIn(stakeAddressList)
        .stream()
        .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity()));

    List<EpochStakeCheckpoint> epochStakeCheckpoints = accountHistoryList
        .stream()
        .filter(
            accountEpochStakes -> !epochStakeCheckpointMap.containsKey(
                accountEpochStakes.getStakeAddress()))
        .map(accountEpochStakes -> EpochStakeCheckpoint.builder()
            .stakeAddress(accountEpochStakes.getStakeAddress())
            .epochCheckpoint(0)
            .build())
        .collect(Collectors.toList());

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

  private List<AccountHistory> getAccountHistoryList(List<String> stakeAddressList)
          throws ApiException {
    return koiosClient.accountService()
            .getAccountHistory(stakeAddressList, null, null)
            .getValue();
  }

  private List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList, int currentEpoch) {
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

}
