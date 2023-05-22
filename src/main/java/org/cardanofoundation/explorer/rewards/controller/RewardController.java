package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.Reward3ConcurrentFetching;
import org.cardanofoundation.explorer.rewards.service.Reward3FetchingService;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

  private final Reward3ConcurrentFetching reward3ConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchRewards(@RequestBody Set<String> stakeAddressSet) {
    return reward3ConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
