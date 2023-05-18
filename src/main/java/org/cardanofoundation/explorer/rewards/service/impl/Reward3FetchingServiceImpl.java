package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.entity.RewardCheckpoint;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.Reward3Repository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.repository.RewardCheckpointRepository;
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

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;
  final Reward3Repository reward3Repository;
  final RewardCheckpointRepository rewardCheckpointRepository;
  final EpochRepository epochRepository;

  @Override
  @Transactional
  public Boolean fetchData(List<String> stakeAddressList) {
    var curTime = System.currentTimeMillis();
    try {
      Integer currentEpoch = epochRepository.findMaxEpoch();

      List<AccountRewards> accountRewardsList = getAccountRewards(stakeAddressList);

      Map<String, RewardCheckpoint> rewardCheckpointMap = getRewardCheckpointMap(stakeAddressList,
                                                                                 accountRewardsList);

      Map<String, StakeAddress> stakeAddressMap = getStakeAddressMap(stakeAddressList);

      Map<String, PoolHash> poolHashMap = getPoolHashMap(accountRewardsList);

      List<Reward3> result = new ArrayList<>();

      for (var accountRewards : accountRewardsList) {
        var rewardCheckpoint = rewardCheckpointMap.get(accountRewards.getStakeAddress());
        if (rewardCheckpoint == null) {
          continue;
        }

        for (var accountReward : accountRewards.getRewards()) {
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

      log.info("Fetch {} reward record by koios api: {} ms, with stake_address input size {}",
               result.size(), System.currentTimeMillis() - curTime,
               stakeAddressList.size());
    } catch (ApiException e) {
      //todo
      log.error("Exception when fetching reward data", e);
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
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
}
