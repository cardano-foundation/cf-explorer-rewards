package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.common.entity.ledgersync.Epoch;
import org.cardanofoundation.explorer.rewards.concurrent.EpochConcurrentFetching;

@RestController
@RequestMapping("/api/v1/epochs")
@RequiredArgsConstructor
@Profile("koios")
public class EpochController {

  private final EpochConcurrentFetching epochConcurrentFetching;

  @PostMapping("/fetch")
  public List<Epoch> fetchEpochs(@RequestBody Set<Integer> epochs) {
    return epochConcurrentFetching.fetchDataConcurrently(new ArrayList<>(epochs));
  }
}
