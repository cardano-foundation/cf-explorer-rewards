package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
import org.cardanofoundation.explorer.rewards.entity.EpochStakeCheckpoint;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.EpochStake3Repository;
import org.cardanofoundation.explorer.rewards.repository.EpochStakeCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.EpochStake3FetchingService;
import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.account.model.AccountHistoryInner;
import rest.koios.client.backend.api.base.exception.ApiException;

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
  @Transactional
  @Async
  public CompletableFuture<Boolean> fetchData(List<String> stakeAddressList) {
    List<EpochStake3> result = new ArrayList<>();
    try {
      var curTime = System.currentTimeMillis();
      Integer currentEpoch = epochRepository.findMaxEpoch();

      List<AccountHistory> accountHistoryList = koiosClient.accountService()
          .getAccountHistory(stakeAddressList, null, null)
          .getValue();

      int epochStakeSize = accountHistoryList
          .parallelStream()
          .mapToInt(accountHistory -> accountHistory.getHistory().size())
          .sum();

      log.info("fetch {} epoch_stake by koios api: {} ms, with stake_address input size {}",
          epochStakeSize, System.currentTimeMillis() - curTime,
          stakeAddressList.size());

      List<String> poolIds = accountHistoryList.stream()
          .flatMap(accountHistory -> accountHistory.getHistory().stream())
          .map(AccountHistoryInner::getPoolId)
          .toList();

      Map<String, PoolHash> poolHashMap = poolHashRepository.findByViewIn(poolIds).stream()
          .collect(Collectors.toMap(PoolHash::getView, Function.identity()));

      Map<String, StakeAddress> stakeAddressMap = stakeAddressRepository
          .findByViewIn(stakeAddressList)
          .stream()
          .collect(Collectors.toMap(StakeAddress::getView, Function.identity()));

      Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap =
          getEpochStakeCheckpointMap(stakeAddressList, accountHistoryList);

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
          result.size(), System.currentTimeMillis() - curTime, stakeAddressList.size());
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
}
