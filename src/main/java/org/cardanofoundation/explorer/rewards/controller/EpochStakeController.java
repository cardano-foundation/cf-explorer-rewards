package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.service.EpochStake3FetchingService;

@RestController
@RequestMapping("/api/v1/epoch-stake")
@RequiredArgsConstructor
public class EpochStakeController {
  private final EpochStake3FetchingService epochStake3FetchingService;

  @PostMapping("/fetch")
  public Boolean fetchRewards(@RequestBody Set<String> stakeAddressSet) {
    return epochStake3FetchingService.fetchData(new ArrayList<>(stakeAddressSet));
  }
}
