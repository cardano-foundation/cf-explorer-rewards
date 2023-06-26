package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.EpochStakeConcurrentFetching;

@RestController
@RequestMapping("/api/v1/epoch-stake")
@RequiredArgsConstructor
@Profile("koios")
public class EpochStakeController {

  private final EpochStakeConcurrentFetching epochStakeConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchEpochStakes(@RequestBody Set<String> stakeAddressSet) {
    return epochStakeConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
