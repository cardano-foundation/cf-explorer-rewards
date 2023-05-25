package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.EpochStake3ConcurrentFetching;

@RestController
@RequestMapping("/api/v1/epoch-stake")
@RequiredArgsConstructor
public class EpochStakeController {
  private final EpochStake3ConcurrentFetching epochStake3ConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchEpochStakes(@RequestBody Set<String> stakeAddressSet) {
    return epochStake3ConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
