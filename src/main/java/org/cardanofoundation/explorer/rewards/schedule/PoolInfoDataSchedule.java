package org.cardanofoundation.explorer.rewards.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.common.entity.ledgersync.PoolHash;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.schedule.service.PoolInfoDataService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "application.pool-info.job.enable",
    matchIfMissing = true,
    havingValue = "true")
@Profile("koios")
public class PoolInfoDataSchedule {

  final PoolHashRepository poolHashRepository;
  final PoolInfoDataService poolInfoDataService;

  @Setter
  @Value("${application.pool-info.list-size-each-thread}")
  int subListSize;

  @Scheduled(fixedDelayString = "${application.pool-info.job.fixed-delay}")
  @SneakyThrows
  public void fetchAllPoolInfoData() {
    log.info("Pool Info Job:-------------Start job---------------");
    var curTime = System.currentTimeMillis();
    var poolHashList = poolHashRepository.findAll();
    var poolIds = poolHashList.stream().map(PoolHash::getView).collect(Collectors.toList());

    // fetch and store data concurrently
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < poolIds.size(); i += subListSize) {
      int endIndex = Math.min(i + subListSize, poolIds.size());
      var sublist = poolIds.subList(i, endIndex);

      CompletableFuture<Boolean> future =
          poolInfoDataService
              .fetchData(sublist)
              .exceptionally(
                  ex -> {
                    log.error(
                        "Exception occurred in fetchData for poolId {}: {}",
                        sublist,
                        ex.getMessage());
                    return Boolean.FALSE;
                  });
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
    boolean result = futures.stream().allMatch(CompletableFuture::join);

    if (result) {
      log.info(
          "Pool Info Job: It's success, fetch and save pool info record concurrently by koios api: {} ms",
          System.currentTimeMillis() - curTime);
    } else {
      log.info("Pool Info Job: It's not success");
    }
  }
}
