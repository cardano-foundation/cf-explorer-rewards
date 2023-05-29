package org.cardanofoundation.explorer.rewards.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.cardanofoundation.explorer.consumercommon.entity.BaseEntity;
import org.hibernate.Hibernate;

@Entity
@Table(name = "reward_checkpoint_2", uniqueConstraints = {
    @UniqueConstraint(name = "unique_stake_address_checkpoint_2",
        columnNames = {"view"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RewardCheckpoint2 extends BaseEntity {
  @Column(name = "view", nullable = false)
  private String stakeAddress;

  @Column(name = "epoch_checkpoint", nullable = false)
  private Integer epochCheckpoint;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    RewardCheckpoint2 rewardCheckpoint = (RewardCheckpoint2) o;
    return id != null && Objects.equals(id, rewardCheckpoint.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
