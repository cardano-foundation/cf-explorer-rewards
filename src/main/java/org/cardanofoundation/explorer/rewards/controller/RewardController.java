package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.RewardConcurrentFetching;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

  private final RewardConcurrentFetching rewardConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchRewards(@RequestBody Set<String> stakeAddressSet) {
    return rewardConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
