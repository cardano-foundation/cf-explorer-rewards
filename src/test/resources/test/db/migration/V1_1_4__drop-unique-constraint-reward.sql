ALTER TABLE reward DROP CONSTRAINT IF EXISTS unique_reward;
ALTER INDEX IF EXISTS unique_reward_2 RENAME TO unique_reward;