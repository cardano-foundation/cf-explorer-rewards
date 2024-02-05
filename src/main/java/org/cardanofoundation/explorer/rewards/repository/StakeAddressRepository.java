package org.cardanofoundation.explorer.rewards.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;

@Repository
public interface StakeAddressRepository extends JpaRepository<StakeAddress, Long> {

  List<StakeAddress> findByViewIn(Collection<String> views);
}
