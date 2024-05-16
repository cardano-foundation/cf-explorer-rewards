CREATE INDEX IF NOT EXISTS composite_idx_pool_info_active_stake_live_saturation on pool_info (active_stake, live_saturation);
CREATE INDEX IF NOT EXISTS idx_pool_info_active_stake on pool_info (active_stake);
CREATE INDEX IF NOT EXISTS idx_pool_info_live_saturation on pool_info (live_saturation);
CREATE INDEX IF NOT EXISTS idx_pool_info_live_stake on pool_info (live_stake);