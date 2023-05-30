package org.cardanofoundation.explorer.rewards.controller;

import java.util.ArrayList;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.cardanofoundation.explorer.rewards.concurrent.PoolInfoConcurrentFetching;
import rest.koios.client.backend.api.base.exception.ApiException;

@RestController
@RequestMapping("/api/v1/pool-info")
@RequiredArgsConstructor
public class PoolInfoController {

  private final PoolInfoConcurrentFetching poolInfoConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchPoolInfoList(@RequestBody Set<String> poolIds) throws ApiException {
    return poolInfoConcurrentFetching.fetchDataConcurrently(new ArrayList<>(poolIds));
  }
}
