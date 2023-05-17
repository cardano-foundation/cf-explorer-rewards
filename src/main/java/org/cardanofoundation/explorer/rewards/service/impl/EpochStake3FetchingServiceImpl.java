package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.constant.CommonConstants;
import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.repository.StakeAddressRepository;
import org.cardanofoundation.explorer.rewards.service.interfaces.EpochStake3FetchingService;
import rest.koios.client.backend.api.account.model.AccountHistory;
import rest.koios.client.backend.api.base.Result;
import rest.koios.client.backend.api.base.exception.ApiException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EpochStake3FetchingServiceImpl implements EpochStake3FetchingService {

  final StakeAddressRepository stakeAddressRepository;
  final KoiosClient koiosClient;
  final PoolHashRepository poolHashRepository;

  @Override
  public List<EpochStake3> fetchBatch(Integer start) {
    List<EpochStake3> result = new ArrayList<>();
    while (true) {
      Page<StakeAddress> stakeAddressPage = stakeAddressRepository
          .findAll(PageRequest.of(start, CommonConstants.STAKE_ADDRESS_PAGE_SIZE, Sort.by("id")));

      if (stakeAddressPage.isEmpty()) {
        break;
      }
      List<String> addresses = stakeAddressPage.toList().stream().map(StakeAddress::getView)
          .toList();
      result.addAll(fetchData(addresses));
      start++;
    }
    return result;
  }

  @Override
  public List<EpochStake3> fetchData(List<String> stakeAddresses) {
    List<EpochStake3> result = new ArrayList<>();
    try {
      Result<List<AccountHistory>> accountHistoryRes =
          koiosClient.accountService().getAccountHistory(stakeAddresses, null, null);
      if (!accountHistoryRes.isSuccessful()) {
        //todo
      }
      List<AccountHistory> accountHistoryList = accountHistoryRes.getValue();

      accountHistoryList.forEach(
          accountHistory -> result.addAll(buildEpochStakeList(accountHistory)));
    } catch (ApiException e) {
      //todo
      throw new RuntimeException(e);
    }

    return result;
  }

  private List<EpochStake3> buildEpochStakeList(AccountHistory accountHistory) {
    List<EpochStake3> epochStakes = new ArrayList<>();

    var accountHistoryList = accountHistory.getHistory();
    for (var accountHistoryInner : accountHistoryList) {
      var item = new EpochStake3();
      item.setEpochNo(accountHistoryInner.getEpochNo());
      var poolHash = poolHashRepository.findByView(accountHistoryInner.getPoolId());
      var stakeAddress = stakeAddressRepository.findByView(accountHistory.getStakeAddress());
      if (poolHash.isEmpty() || stakeAddress.isEmpty()) {
        break;
      }
      item.setAddr(stakeAddress.get());
      item.setPool(poolHash.get());
      item.setAmount(new BigInteger(accountHistoryInner.getActiveStake()));

      epochStakes.add(item);
    }

    return epochStakes;
  }
}
