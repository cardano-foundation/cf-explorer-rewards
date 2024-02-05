package org.cardanofoundation.explorer.rewards.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rest.koios.client.backend.api.network.model.Totals;

import org.cardanofoundation.explorer.consumercommon.entity.AdaPots;
import org.cardanofoundation.explorer.consumercommon.entity.Block;
import org.cardanofoundation.explorer.rewards.config.KoiosClient;
import org.cardanofoundation.explorer.rewards.repository.AdaPotsRepository;
import org.cardanofoundation.explorer.rewards.repository.BlockRepository;
import org.cardanofoundation.explorer.rewards.repository.EpochRepository;
import org.cardanofoundation.explorer.rewards.service.AdaPotsFetchingService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Profile("koios")
public class AdaPotsFetchingServiceImpl implements AdaPotsFetchingService {

  final AdaPotsRepository adaPotsRepository;
  final BlockRepository blockRepository;
  final EpochRepository epochRepository;
  final KoiosClient koiosClient;

  @Override
  @Transactional(rollbackFor = {Exception.class})
  @Async
  @SneakyThrows
  public CompletableFuture<Boolean> fetchData(Integer epoch) {
    var adaPotsKoios =
        koiosClient.networkService().getHistoricalTokenomicStatsByEpoch(epoch).getValue();

    adaPotsRepository.save(mapToAdaPots(adaPotsKoios));
    return CompletableFuture.completedFuture(Boolean.TRUE);
  }

  @Override
  public List<Integer> getEpochsNeedFetchData(List<Integer> epochs) {
    Integer currentEpoch = epochRepository.findMaxEpoch();

    List<Integer> existedAdaPosts =
        adaPotsRepository.findByEpochNoIn(epochs).stream().map(AdaPots::getEpochNo).toList();

    return epochs.stream()
        .filter(epoch -> !existedAdaPosts.contains(epoch) && epoch <= currentEpoch)
        .toList();
  }

  private AdaPots mapToAdaPots(Totals totals) {
    Block block = blockRepository.getFirstBlockByEpochNo(totals.getEpochNo());

    return AdaPots.builder()
        .epochNo(totals.getEpochNo())
        .treasury(new BigInteger(totals.getTreasury()))
        .reserves(new BigInteger(totals.getReserves()))
        .rewards(new BigInteger(totals.getReward()))
        .utxo(new BigInteger(totals.getCirculation()))
        .block(block)
        .blockId(block.getId())
        .slotNo(block.getSlotNo())
        .deposits(BigInteger.ZERO) // TODO: will be handled later
        .fees(BigInteger.ZERO) // TODO: will be handled later
        .build();
  }
}
