package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.constant.CommonConstants;
import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.interfaces.Reward3FetchingService;
import rest.koios.client.backend.api.account.model.AccountRewards;
import rest.koios.client.backend.api.base.Result;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class Reward3FetchingServiceImpl implements Reward3FetchingService {

  final KoiosClient koiosClient;
  final StakeAddressRepository stakeAddressRepository;
  final PoolHashRepository poolHashRepository;

  @Override
  public List<Reward3> fetchBatch(Integer start) {
    List<Reward3> result = new ArrayList<>();
    while (true) {
      var stakeAddressPage = stakeAddressRepository
          .findAll(PageRequest.of(start, CommonConstants.STAKE_ADDRESS_PAGE_SIZE, Sort.by("id")));

      if (stakeAddressPage.isEmpty()) {
        break;
      }
      List<String> addresses = stakeAddressPage.toList().stream().map(StakeAddress::getView)
          .toList();
      result.addAll(fetchData(addresses));
      start = start + 1;
      break;
    }
    return result;
  }

  @Override
  public List<Reward3> fetchData(List<String> stakeAddresses) {
    List<Reward3> result = new ArrayList<>();

    try {
      Result<List<AccountRewards>> accountRewardsRes =
          koiosClient.accountService().getAccountRewards(stakeAddresses, null, null);
      if (!accountRewardsRes.isSuccessful()) {
        //todo
        log.error("fetching reward data is not successful");
      }
      List<AccountRewards> accountRewardsList = accountRewardsRes.getValue();

      accountRewardsList.forEach(accountRewards -> {
        result.addAll(buildRewardList(accountRewards));
      });
    } catch (ApiException e) {
      //todo
      log.error("Exception when fetching reward data", e);
      throw new RuntimeException(e);
    }

    return result;
  }

  private List<Reward3> buildRewardList(AccountRewards accountRewards) {
    List<Reward3> result = new ArrayList<>();

    var rewards = accountRewards.getRewards();
    for (var accountReward : rewards) {
      Reward3 item = new Reward3();
      var poolHash = poolHashRepository.findByView(accountReward.getPoolId());
      var stakeAddress = stakeAddressRepository.findByView(accountRewards.getStakeAddress());
      if (poolHash.isEmpty() || stakeAddress.isEmpty()) {
        break;
      }
      item.setPool(poolHash.get());
      item.setAddr(stakeAddress.get());
      item.setAmount(new BigInteger(accountReward.getAmount()));
      item.setEarnedEpoch(accountReward.getEarnedEpoch());
      item.setSpendableEpoch(accountReward.getSpendableEpoch());
      item.setPoolId(poolHash.get().getId());
      item.setStakeAddressId(stakeAddress.get().getId());
      item.setType(RewardType.fromValue(accountReward.getType()));

      result.add(item);
    }

    return result;
  }
}
