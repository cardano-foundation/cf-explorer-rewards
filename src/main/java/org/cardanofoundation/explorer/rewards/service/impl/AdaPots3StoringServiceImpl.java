package org.cardanofoundation.explorer.rewards.service.impl;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import org.cardanofoundation.explorer.rewards.entity.AdaPots3;
import org.cardanofoundation.explorer.rewards.repository.AdaPots3Repository;
import org.cardanofoundation.explorer.rewards.service.interfaces.AdaPots3StoringService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdaPots3StoringServiceImpl implements AdaPots3StoringService {

  final AdaPots3Repository adaPotsRepository;

  @Override
  public void saveBatch(List<AdaPots3> adaPotsList) {
    //TODO handle existed data
    adaPotsRepository.saveAll(adaPotsList);
  }
}
