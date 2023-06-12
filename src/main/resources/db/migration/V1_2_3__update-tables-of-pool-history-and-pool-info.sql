TRUNCATE TABLE pool_history;
ALTER SEQUENCE pool_history_id_seq RESTART WITH 1;
TRUNCATE TABLE pool_info;
ALTER SEQUENCE pool_info_id_seq RESTART WITH 1;
TRUNCATE TABLE pool_history_checkpoint;
ALTER SEQUENCE pool_history_checkpoint_id_seq RESTART WITH 1;
TRUNCATE TABLE pool_info_checkpoint;
ALTER SEQUENCE pool_info_checkpoint_id_seq RESTART WITH 1;


ALTER TABLE pool_history ALTER COLUMN pool_id TYPE int8 USING pool_id::bigint;
ALTER TABLE pool_history ADD CONSTRAINT pool_history_pool_id_fkey FOREIGN KEY (pool_id)
    REFERENCES pool_hash(id);
ALTER TABLE pool_history ALTER COLUMN active_stake TYPE numeric(20) USING active_stake::numeric(20,0);
ALTER TABLE pool_history ALTER COLUMN fixed_cost TYPE numeric(20) USING fixed_cost::numeric(20,0);
ALTER TABLE pool_history ALTER COLUMN deleg_rewards TYPE numeric(20) USING deleg_rewards::numeric(20,0);


ALTER TABLE pool_info ALTER COLUMN pool_id TYPE int8 USING pool_id::bigint;
ALTER TABLE pool_info ADD CONSTRAINT pool_info_pool_id_fkey FOREIGN KEY (pool_id) REFERENCES pool_hash(id);
ALTER TABLE pool_info ALTER COLUMN active_stake TYPE numeric(20) USING active_stake::numeric(20,0);
ALTER TABLE pool_info ALTER COLUMN live_stake TYPE numeric(20) USING live_stake::numeric(20,0);


ALTER TABLE pool_history_checkpoint
    RENAME COLUMN earned_reward TO is_spendable_reward;