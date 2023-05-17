package org.cardanofoundation.explorer.rewards.entity;

import java.math.BigInteger;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.cardanofoundation.explorer.consumercommon.entity.BaseEntity;
import org.cardanofoundation.explorer.consumercommon.entity.PoolHash;
import org.cardanofoundation.explorer.consumercommon.entity.StakeAddress;
import org.cardanofoundation.explorer.consumercommon.enumeration.RewardType;
import org.cardanofoundation.explorer.consumercommon.validation.Lovelace;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "reward3", uniqueConstraints = {
    @UniqueConstraint(name = "unique_reward",
        columnNames = {"addr_id", "type", "earned_epoch", "pool_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Reward3 extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "reward3_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress addr;

  @Column(name = "addr_id", updatable = false, insertable = false)
  private Long stakeAddressId;

  @Column(name = "type", nullable = false)
  private RewardType type;

  @Column(name = "amount", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger amount;

  @Column(name = "earned_epoch", nullable = false)
  private Integer earnedEpoch;

  @Column(name = "spendable_epoch", nullable = false)
  private Integer spendableEpoch;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_id",
      foreignKey = @ForeignKey(name = "reward_pool_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash pool;

  @Column(name = "pool_id", updatable = false, insertable = false)
  private Long poolId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Reward3 reward = (Reward3) o;
    return id != null && Objects.equals(id, reward.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
