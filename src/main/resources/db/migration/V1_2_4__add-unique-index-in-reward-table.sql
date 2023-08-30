CREATE UNIQUE INDEX unique_reward_2 ON reward USING btree (addr_id, type, earned_epoch, coalesce(pool_id, -1));
