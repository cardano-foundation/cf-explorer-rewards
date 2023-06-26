package org.cardanofoundation.explorer.rewards.controller;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.explorer.rewards.concurrent.AdaPostsConcurrentFetching;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/ada-pots")
@RequiredArgsConstructor
@Profile("koios")
public class AdaPotsController {

  private final AdaPostsConcurrentFetching adaPostsConcurrentFetching;

  @PostMapping("/fetch")
  public Boolean fetchAdaPots(@RequestBody Set<Integer> epochs) {
    return adaPostsConcurrentFetching.fetchDataConcurrently(new ArrayList<>(epochs));
  }

}
