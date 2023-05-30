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
@Table(name = "pool_info", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_info",
        columnNames = {"pool_id", "fetched_at_epoch"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolInfo extends BaseEntity {

  @Column(name = "pool_id", nullable = false)
  private String poolId;
  @Column(name = "fetched_at_epoch", nullable = false)
  private Integer fetchedAtEpoch;
  @Column(name = "active_stake", nullable = false)
  private String activeStake;
  @Column(name = "live_stake", nullable = false)
  private String liveStake;
  @Column(name = "live_saturation", nullable = false)
  private Double liveSaturation;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolInfo poolInfo = (PoolInfo) o;
    return id != null && Objects.equals(id, poolInfo.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
