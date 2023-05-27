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
@Table(name = "pool_history", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_history",
        columnNames = {"pool_id", "epoch_no"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolHistory extends BaseEntity {

  @Column(name = "pool_id", nullable = false)
  private String poolId;

  @Column(name = "epoch_no")
  private Integer epochNo;

  @Column(name = "active_stake")
  private String activeStake;

  @Column(name = "active_stake_pct")
  private Double activeStakePct;

  @Column(name = "saturation_pct")
  private Double saturationPct;

  @Column(name = "block_cnt")
  private Integer blockCnt;

  @Column(name = "delegator_cnt")
  private Integer delegatorCnt;

  @Column(name = "margin")
  private Double margin;

  @Column(name = "fixed_cost")
  private String fixedCost;

  @Column(name = "pool_fees")
  private String poolFees;

  @Column(name = "deleg_rewards")
  private String delegRewards;

  @Column(name = "epoch_ros")
  private Double epochRos;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolHistory poolHistory = (PoolHistory) o;
    return id != null && Objects.equals(id, poolHistory.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
