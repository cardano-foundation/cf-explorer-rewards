package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.Reward4ConcurrentFetching;
import org.cardanofoundation.explorer.rewards.concurrent.RewardConcurrentFetching;
import rest.koios.client.backend.api.base.exception.ApiException;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class Reward4Controller {
  private final Reward4ConcurrentFetching rewardConcurrentFetching;

  @PostMapping("/fetch2")
  public Boolean fetchRewards(@RequestBody Set<String> stakeAddressSet)
      throws ApiException, InterruptedException {
    return rewardConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
