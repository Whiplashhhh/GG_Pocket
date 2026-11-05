-- Security review (D-112): a deleted comment is shown nowhere, its text has no reason to stay in the
-- database and the backups. From now on the app erases it on deletion; this erases the older ones.
update session_comment set body = '' where deleted_at is not null and body <> '';
update route_comment set body = '' where deleted_at is not null and body <> '';
