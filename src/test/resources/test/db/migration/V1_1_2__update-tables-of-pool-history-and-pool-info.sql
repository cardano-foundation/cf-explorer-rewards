ALTER TABLE pool_history ALTER COLUMN pool_id TYPE int8 USING pool_id::bigint;

ALTER TABLE pool_history ALTER COLUMN active_stake TYPE numeric(20) USING active_stake::numeric(20,0);
ALTER TABLE pool_history ALTER COLUMN fixed_cost TYPE numeric(20) USING fixed_cost::numeric(20,0);
ALTER TABLE pool_history ALTER COLUMN deleg_rewards TYPE numeric(20) USING deleg_rewards::numeric(20,0);
ALTER TABLE pool_history ALTER COLUMN pool_fees TYPE numeric(20) USING pool_fees::numeric(20,0);

ALTER TABLE pool_info ALTER COLUMN pool_id TYPE int8 USING pool_id::bigint;

ALTER TABLE pool_info ALTER COLUMN active_stake TYPE numeric(20) USING active_stake::numeric(20,0);
ALTER TABLE pool_info ALTER COLUMN live_stake TYPE numeric(20) USING live_stake::numeric(20,0);


ALTER TABLE pool_history_checkpoint
    RENAME COLUMN earned_reward TO is_spendable_reward;