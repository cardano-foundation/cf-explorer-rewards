package org.cardanofoundation.explorer.rewards.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.rewards.repository.*;
import org.cardanofoundation.explorer.rewards.service.Reward3FetchingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rest.koios.client.backend.api.account.model.AccountReward;
import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.exception.ApiException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class Reward3FetchingServiceImpl implements Reward3FetchingService {

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;
  final Reward3Repository reward3Repository;
  final RewardCheckpointRepository rewardCheckpointRepository;
  final EpochRepository epochRepository;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  public CompletableFuture<Boolean> fetchData(List<String> stakeAddressList) {
    var curTime = System.currentTimeMillis();
    try {

      Integer currentEpoch = epochRepository.findMaxEpoch();
      // we only fetch data with addresses that are not in the checkpoint table
      // or in the checkpoint table but have an epoch checkpoint value < (current epoch - 1)
      List<String> stakeAddressListNeedFetchData = getStakeAddressListNeedFetchData(stakeAddressList, currentEpoch);

      if (stakeAddressListNeedFetchData.isEmpty()) {
        log.info("Reward: all stake addresses were in checkpoint and had epoch checkpoint = current epoch - 1");
        return CompletableFuture.completedFuture(true);
      }

      List<AccountRewards> accountRewardsList = getAccountRewards(stakeAddressListNeedFetchData);

      int rewardSize = accountRewardsList
          .parallelStream()
          .mapToInt(accountHistory -> accountHistory.getRewards().size())
          .sum();

      log.info("fetch {} reward by koios api: {} ms, with stake_address input size {}",
          rewardSize, System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());

      Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(stakeAddressListNeedFetchData,
          accountRewardsList);

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressListNeedFetchData);

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountRewardsList);

      List<Reward3> result = new ArrayList<>();

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

          Reward3 reward3 = Reward3.builder()
              .pool(poolHashMap.get(accountReward.getPoolId()))
              .addr(stakeAddressMap.get(accountRewards.getStakeAddress()))
              .amount(new BigInteger(accountReward.getAmount()))
              .earnedEpoch(accountReward.getEarnedEpoch())
              .spendableEpoch(accountReward.getSpendableEpoch())
              .type(RewardType.fromValue(accountReward.getType()))
              .build();

          result.add(reward3);
        }
      }

      rewardCheckpointMap
          .values()
          .forEach(rewardCheckpoint -> rewardCheckpoint.setEpochCheckpoint(currentEpoch - 1));

      reward3Repository.saveAll(result);
      rewardCheckpointRepository.saveAll(rewardCheckpointMap.values());

      log.info("Save {} reward record from koios api: {} ms, with stake_address input size {}",
          result.size(), System.currentTimeMillis() - curTime, stakeAddressListNeedFetchData.size());
    } catch (ApiException e) {
      log.error("Exception when fetching reward data", e);
      return CompletableFuture.completedFuture(Boolean.FALSE);
    }

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

   private List<String> getStakeAddressListNeedFetchData(List<String> stakeAddressList, int currentEpoch) {
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
