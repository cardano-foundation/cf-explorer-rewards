package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.RewardConcurrentFetching;
import rest.koios.client.backend.api.base.exception.ApiException;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Profile("koios")
public class RewardController {

  private final RewardConcurrentFetching rewardConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchRewards(@RequestBody Set<String> stakeAddressSet)
      throws ApiException {
    return rewardConcurrentFetching.fetchDataConcurrently(new ArrayList<>(stakeAddressSet));
  }
}
