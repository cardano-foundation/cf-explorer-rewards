package org.cardanofoundation.explorer.rewards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cardanofoundation.explorer.rewards.entity.AdaPots3;

@Repository
public interface AdaPots3Repository extends JpaRepository<AdaPots3, Long> {

}
