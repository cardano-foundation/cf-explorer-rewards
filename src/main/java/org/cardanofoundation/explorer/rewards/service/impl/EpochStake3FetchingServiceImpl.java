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

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
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

  @Override
  public List<EpochStake3> fetchData(List<String> stakeAddressList) {
    List<EpochStake3> result = new ArrayList<>();
    try {
      var curTime = System.currentTimeMillis();

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

      accountHistoryList.parallelStream().forEach(accountHistory -> {
        for (AccountHistoryInner accountHistoryInner : accountHistory.getHistory()) {
          var item = new EpochStake3();
          item.setEpochNo(accountHistoryInner.getEpochNo());
          item.setAddr(stakeAddressMap.get(accountHistory.getStakeAddress()));
          item.setPool(poolHashMap.get(accountHistoryInner.getPoolId()));
          item.setAmount(new BigInteger(accountHistoryInner.getActiveStake()));
          result.add(item);
        }
      });
      log.info("fetch {} epoch_stake complete in: {} ms, with stake_address input size {}");
    } catch (ApiException e) {
      //todo
      throw new RuntimeException(e);
    }

    return result;
  }
}
