package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.PoolHistoryConcurrentFetching;
import rest.koios.client.backend.api.base.exception.ApiException;

@RestController
@RequestMapping("/api/v1/pool-history")
@RequiredArgsConstructor
@Profile("koios")
public class PoolHistoryController {

  private final PoolHistoryConcurrentFetching poolHistoryConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchPoolHistoryList(@RequestBody Set<String> poolIds) throws ApiException {
    return poolHistoryConcurrentFetching.fetchDataConcurrently(new ArrayList<>(poolIds));
  }
}
