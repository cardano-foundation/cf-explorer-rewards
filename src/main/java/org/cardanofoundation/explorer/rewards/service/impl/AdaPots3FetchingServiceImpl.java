package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.entity.AdaPots3;
import org.cardanofoundation.explorer.rewards.service.interfaces.AdaPots3FetchingService;
import rest.koios.client.backend.api.base.Result;
import rest.koios.client.backend.api.base.exception.ApiException;
import rest.koios.client.backend.api.network.model.Totals;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class AdaPots3FetchingServiceImpl implements AdaPots3FetchingService {

  final KoiosClient koiosClient;

  @Override
  public List<AdaPots3> fetchData() {
    List<AdaPots3> result = new ArrayList<>();

    try {
      Result<List<Totals>> totalsRes = koiosClient.networkService()
          .getHistoricalTokenomicStats(null);
      if (!totalsRes.isSuccessful()) {
        log.error("fetching ada pots data is not successful");
      }
      List<Totals> totalsList = totalsRes.getValue();
      totalsList.forEach(
          totals -> result.add(buildAdaPots(totals))
      );
    } catch (ApiException e) {
      //todo
      throw new RuntimeException(e);
    }

    return result;
  }

  private AdaPots3 buildAdaPots(Totals total) {
    var adaPots = new AdaPots3();
    adaPots.setEpochNo(total.getEpochNo());
    adaPots.setTreasury(new BigInteger(total.getTreasury()));
    adaPots.setReserves(new BigInteger(total.getReserves()));
    adaPots.setRewards(new BigInteger(total.getReward()));

    return adaPots;
  }

}
