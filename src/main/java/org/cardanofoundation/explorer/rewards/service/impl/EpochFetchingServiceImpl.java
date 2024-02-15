package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.common.util.StringUtils;

import org.cardanofoundation.explorer.common.entity.enumeration.EraType;
import org.cardanofoundation.explorer.common.entity.ledgersync.Epoch;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.service.EpochFetchingService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class EpochFetchingServiceImpl implements EpochFetchingService {

  final KoiosClient koiosClient;
  final EpochRepository epochRepository;

  static final Integer NUMBER_EPOCH_CALC_AND_DELIVER_REWARD = 2;

  @Override
  @Async
  @Transactional(rollbackFor = {Exception.class})
  @SneakyThrows
  public CompletableFuture<Epoch> fetchData(Integer epochNo) {
    Epoch epoch = epochRepository.findByNo(epochNo).orElse(null);
    if (Objects.isNull(epoch)
        || Objects.nonNull(epoch.getRewardsDistributed())
        || epoch.getEra().equals(EraType.BYRON)
        || epoch.getEra().equals(EraType.BYRON_EBB)) {
      return null;
    }
    String totalRewards =
        koiosClient.epochService().getEpochInformationByEpoch(epochNo).getValue().getTotalRewards();
    if (StringUtils.isEmpty(totalRewards)) {
      Integer currentEpoch = epochRepository.findMaxEpoch();
      if (epoch.getNo() <= currentEpoch - NUMBER_EPOCH_CALC_AND_DELIVER_REWARD) {
        totalRewards = BigInteger.ZERO.toString();
      } else {
        return CompletableFuture.completedFuture(epoch);
      }
    }
    BigInteger rewardDistributed = new BigInteger(totalRewards);
    epochRepository.updateRewardDistributedByNo(rewardDistributed, epochNo);
    epoch.setRewardsDistributed(rewardDistributed);
    return CompletableFuture.completedFuture(epoch);
  }

  @Override
  public List<Integer> getEpochsNeedFetchData(List<Integer> epochNoList) {

    Integer currentEpoch = epochRepository.findMaxEpoch();

    List<Integer> epochContainsRewardDistributed =
        epochRepository.findByRewardsDistributedIsNotNull().stream().map(Epoch::getNo).toList();
    return epochNoList.stream()
        .filter(
            epoch ->
                !epochContainsRewardDistributed.contains(epoch)
                    && epoch <= currentEpoch - NUMBER_EPOCH_CALC_AND_DELIVER_REWARD)
        .toList();
  }
}
