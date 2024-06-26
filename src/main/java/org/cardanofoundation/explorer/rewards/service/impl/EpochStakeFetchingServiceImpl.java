package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.account.model.AccountHistoryInner;
import rest.koios.client.backend.api.base.exception.ApiException;

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
import org.cardanofoundation.explorer.rewards.service.EpochStakeFetchingService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class EpochStakeFetchingServiceImpl implements EpochStakeFetchingService {

  final StakeAddressRepository stakeAddressRepository;
  final KoiosClient koiosClient;
  final PoolHashRepository poolHashRepository;
  final EpochStakeCheckpointRepository epochStakeCheckpointRepository;
  final JOOQEpochStakeRepository jooqEpochStakeRepository;
  final JOOQEpochStakeCheckpointRepository jooqEpochStakeCheckpointRepository;
  final EpochService epochService;

  @Override
  @Transactional(rollbackFor = {Exception.class})
  @Async
  @SneakyThrows
  public CompletableFuture<Boolean> fetchData(List<String> stakeAddressList) {
    var curTime = System.currentTimeMillis();
    int currentEpoch = epochService.getCurrentEpoch();
    List<AccountHistory> accountHistoryList = getAccountHistoryList(stakeAddressList);

    int epochStakeSize =
        accountHistoryList.parallelStream()
            .mapToInt(accountHistory -> accountHistory.getHistory().size())
            .sum();

    log.info(
        "fetch {} epoch_stake by koios api: {} ms, with stake_address input size {}",
        epochStakeSize,
        System.currentTimeMillis() - curTime,
        stakeAddressList.size());

    Map<String, PoolHash> poolHashMap = getPoolHashMap(accountHistoryList);

    Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap =
        getEpochStakeCheckpointMap(stakeAddressList);

    List<EpochStake> saveData =
        accountHistoryList.parallelStream()
            .flatMap(
                accountHistory -> {
                  var epochStakeCheckpoint =
                      epochStakeCheckpointMap.get(accountHistory.getStakeAddress());
                  if (epochStakeCheckpoint == null) {
                    return Stream.empty();
                  }

                  return accountHistory.getHistory().stream()
                      .filter(
                          accountHistoryInner ->
                              accountHistoryInner.getEpochNo()
                                      > epochStakeCheckpoint.getEpochCheckpoint()
                                  && accountHistoryInner.getEpochNo() < currentEpoch + 2)
                      .map(
                          accountHistoryInner ->
                              EpochStake.builder()
                                  .epochNo(accountHistoryInner.getEpochNo())
                                  .addr(stakeAddressMap.get(accountHistory.getStakeAddress()))
                                  .pool(poolHashMap.get(accountHistoryInner.getPoolId()))
                                  .amount(new BigInteger(accountHistoryInner.getActiveStake()))
                                  .build());
                })
            .collect(Collectors.toList());

    epochStakeCheckpointMap
        .values()
        .forEach(epochCheckpoint -> epochCheckpoint.setEpochCheckpoint(currentEpoch));
    jooqEpochStakeRepository.saveAll(saveData);
    jooqEpochStakeCheckpointRepository.saveAll(epochStakeCheckpointMap.values().stream().toList());

    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  private Map<String, EpochStakeCheckpoint> getEpochStakeCheckpointMap(
      List<String> stakeAddressList) {
    // get epochStakeCheckpointMap with stakeAddressList
    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap =
        epochStakeCheckpointRepository.findByStakeAddressIn(stakeAddressList).stream()
            .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity()));

    // if an stake address not in checkpoint table,
    // create a epochStakeCheckpoint with stake address equal to that and epoch_checkpoint = 0
    List<EpochStakeCheckpoint> epochStakeCheckpoints =
        stakeAddressList.stream()
            .filter(stakeAddress -> !epochStakeCheckpointMap.containsKey(stakeAddress))
            .map(
                stakeAddress ->
                    EpochStakeCheckpoint.builder()
                        .stakeAddress(stakeAddress)
                        .epochCheckpoint(0)
                        .build())
            .collect(Collectors.toList());

    // put all into result
    epochStakeCheckpointMap.putAll(
        epochStakeCheckpoints.stream()
            .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity())));

    return epochStakeCheckpointMap;
  }

  private Map<String, StakeAddress> getStakeAddressMap(List<String> stakeAddressList) {
    return stakeAddressRepository.findByViewIn(stakeAddressList).stream()
        .collect(Collectors.toMap(StakeAddress::getView, Function.identity()));
  }

  private Map<String, PoolHash> getPoolHashMap(List<AccountHistory> accountHistoryList) {
    List<String> poolIds =
        accountHistoryList.stream()
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
    return koiosClient.accountService().getAccountHistory(stakeAddressList, null, null).getValue();
  }

  /**
   * get stake address list that are not in the checkpoint table or in the checkpoint table but have
   * an epoch checkpoint value < (current epoch)
   *
   * @param stakeAddressList
   * @return
   */
  @Override
  public List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList)
      throws ApiException {
    Integer currentEpoch = epochService.getCurrentEpoch();

    Map<String, EpochStakeCheckpoint> epochStakeCheckpointMap =
        epochStakeCheckpointRepository.findByStakeAddressIn(stakeAddressList).stream()
            .collect(Collectors.toMap(EpochStakeCheckpoint::getStakeAddress, Function.identity()));

    return stakeAddressList.stream()
        .filter(
            stakeAddress ->
                ((!epochStakeCheckpointMap.containsKey(stakeAddress))
                    || epochStakeCheckpointMap.get(stakeAddress).getEpochCheckpoint()
                        < currentEpoch))
        .collect(Collectors.toList());
  }
}
