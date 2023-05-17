package org.cardanofoundation.explorer.rewards.schedules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.explorer.rewards.entity.EpochStake3;
import org.cardanofoundation.explorer.rewards.entity.Reward3;
import org.cardanofoundation.explorer.rewards.service.interfaces.EpochStake3FetchingService;
import org.cardanofoundation.explorer.rewards.service.interfaces.EpochStake3StoringService;
import org.cardanofoundation.explorer.rewards.service.interfaces.Reward3FetchingService;
import org.cardanofoundation.explorer.rewards.service.interfaces.Reward3StoringService;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConditionalOnProperty(value = "jobs.koios.enabled", matchIfMissing = true, havingValue = "true")
@RequiredArgsConstructor
public class KoiosDataSchedule {

  final EpochStake3StoringService epochStakeStoringService;
  final EpochStake3FetchingService epochStakeFetchingService;
  final Reward3FetchingService rewardFetchingService;
  final Reward3StoringService rewardStoringService;

  @Value("${jobs.save-batch}")
  int batchSize;

  @Scheduled(fixedDelayString = "3000000")
  @Transactional
  public void saveKoiosData() {
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    executorService.execute(this::saveEpochStake);
    executorService.execute(this::saveReward);

    executorService.shutdown();

    try {
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      log.error("InterruptedException occurred", e);
      Thread.currentThread().interrupt();
    }

    log.info("----------END----------");
  }

  public void saveEpochStake() {
    log.info("saveEpochStake start");
    var startTime = System.currentTimeMillis();
    var savingEpochStakes = epochStakeFetchingService.fetchBatch(BigInteger.ZERO.intValue());
    log.info("epochStake size " + savingEpochStakes.size());
    log.info("saveEpochStake Fetching time " + (System.currentTimeMillis() - startTime));
    var batchEpochStakes = new ArrayList<EpochStake3>();

    while (!savingEpochStakes.isEmpty()) {
      batchEpochStakes.add(savingEpochStakes.remove(0));
      if (batchEpochStakes.size() == batchSize) {
        epochStakeStoringService.saveBatch(batchEpochStakes);
        batchEpochStakes = new ArrayList<>();
      }
    }
    if (!batchEpochStakes.isEmpty()) {
      epochStakeStoringService.saveBatch(batchEpochStakes);
    }
    log.info("saveEpochStake End time " + (System.currentTimeMillis() - startTime));
    log.info("saveEpochStake end");
  }

  public void saveReward() {
    log.info("saveReward: start");
    var startTime = System.currentTimeMillis();
    var savingRewards = rewardFetchingService.fetchBatch(BigInteger.ZERO.intValue());
    log.info("rewards size " + savingRewards.size());

    log.info("saveReward fetching Time: " + (System.currentTimeMillis() - startTime));
    var batchRewards = new ArrayList<Reward3>();

    while (!savingRewards.isEmpty()) {
      batchRewards.add(savingRewards.remove(0));
      if (batchRewards.size() == batchSize) {
        rewardStoringService.saveBatch(batchRewards);
        batchRewards = new ArrayList<>();
      }
    }
    if (!batchRewards.isEmpty()) {
      rewardStoringService.saveBatch(batchRewards);
    }
    log.info("saveReward End time " + (System.currentTimeMillis() - startTime));
    log.info("saveReward end");
  }

}
