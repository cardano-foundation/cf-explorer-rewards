package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.cardanofoundation.explorer.consumercommon.entity.Reward;
import org.cardanofoundation.explorer.consumercommon.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.repository.custom.CustomRewardCheckpointRepository;
import org.cardanofoundation.explorer.rewards.repository.custom.CustomRewardRepository;
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

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;
  final RewardCheckpointRepository rewardCheckpointRepository;
  final EpochRepository epochRepository;
  final CustomRewardRepository customRewardRepository;
  final CustomRewardCheckpointRepository customRewardCheckpointRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(List<String> stakeAddressList)
      throws ApiException {
    var curTime = System.currentTimeMillis();
    Integer currentEpoch = epochRepository.findMaxEpoch();

    List<AccountRewards> accountRewardsList = getAccountRewards(stakeAddressList);

    int rewardSize = accountRewardsList
        .parallelStream()
        .mapToInt(accountHistory -> accountHistory.getRewards().size())
        .sum();

    log.info("fetch {} reward by koios api: {} ms, with stake_address input size {}",
        rewardSize, System.currentTimeMillis() - curTime, stakeAddressList.size());

    Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(
        stakeAddressList);

    Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

    Map<String, PoolHash> poolHashMap = getPoolHashMap(accountRewardsList);

    List<Reward> result = new ArrayList<>();

    for (var accountRewards : accountRewardsList) {
      var rewardCheckpoint = rewardCheckpointMap.get(accountRewards.getStakeAddress());
      if (rewardCheckpoint == null) {
        continue;
      }

      for (var accountReward : accountRewards.getRewards()) {
        // if earned epoch <= epoch checkpoint, data was saved
        if (accountReward.getEarnedEpoch() <= rewardCheckpoint.getEpochCheckpoint()) {
          continue;
        }

        Reward reward = Reward.builder()
            .pool(poolHashMap.get(accountReward.getPoolId()))
            .addr(stakeAddressMap.get(accountRewards.getStakeAddress()))
            .amount(new BigInteger(accountReward.getAmount()))
            .earnedEpoch(accountReward.getEarnedEpoch())
            .spendableEpoch(accountReward.getSpendableEpoch())
            .type(RewardType.fromValue(accountReward.getType()))
            .build();

        result.add(reward);
      }
    }

    rewardCheckpointMap
        .values()
        .forEach(rewardCheckpoint -> rewardCheckpoint.setEpochCheckpoint(currentEpoch - 1));

    customRewardRepository.saveRewards(result);
    customRewardCheckpointRepository.saveCheckpoints(
        rewardCheckpointMap.values().stream().toList());

    log.info("Save {} reward record from koios api: {} ms, with stake_address input size {}",
        result.size(), System.currentTimeMillis() - curTime, stakeAddressList.size());

    return CompletableFuture.completedFuture(Boolean.TRUE);
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
    // get rewardCheckpointMap with stakeAddressList
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
   * @return
   */
  @Override
  public List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList) {
    Integer currentEpoch = epochRepository.findMaxEpoch();

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
}
