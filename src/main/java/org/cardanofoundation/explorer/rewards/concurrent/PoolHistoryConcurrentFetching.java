package org.cardanofoundation.explorer.rewards.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.rewards.repository.PoolHashRepository;
import org.cardanofoundation.explorer.rewards.service.PoolHistoryFetchingService;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PoolHistoryConcurrentFetching {

  final PoolHistoryFetchingService poolHistoryFetchingService;
  final PoolHashRepository poolHashRepository;

  public Boolean fetchDataConcurrently(List<String> poolIds) {
    var curTime = System.currentTimeMillis();
    var poolPage = poolHashRepository.findAll(PageRequest.of(0, 50, Sort.by("id")));
    poolIds = poolPage.stream().map(PoolHash::getView).collect(
        Collectors.toList());
    List<String> poolIdListNeedFetchData = poolHistoryFetchingService.getPoolIdListNeedFetchData(
        poolIds);

    if (poolIdListNeedFetchData.isEmpty()) {
      log.info(
          "Reward: all poolId were in checkpoint and had epoch checkpoint = current epoch - 1");
      return Boolean.TRUE;
    }
    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (var poolId : poolIdListNeedFetchData) {
      CompletableFuture<Boolean> future = poolHistoryFetchingService.fetchData(poolId);
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    boolean result = futures.stream().allMatch(CompletableFuture::join);

    log.info("Fetch and save pool history record concurrently by koios api: {} ms",
        System.currentTimeMillis() - curTime);

    return result;
  }
}
