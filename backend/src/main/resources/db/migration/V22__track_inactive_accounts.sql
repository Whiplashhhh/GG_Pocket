-- Inactive accounts (D-119): one year without a visit puts an account to sleep (disabled, the
-- member is told why at sign-in), two years delete it like a deletion of one's own.
-- dormant_since: set when the daily job put the account to sleep; null for an account disabled by
-- the board, or active. reactivated_at: the last time the board enabled the account again, so that
-- the year without a visit starts over from there (last_seen_at stays the true last visit).
alter table member add column dormant_since timestamptz;
alter table member add column reactivated_at timestamptz;
