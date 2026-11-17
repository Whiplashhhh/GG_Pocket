-- The bell (club feedback, D-128): what is new is computed from the reactions, comments, sessions and
-- friend requests themselves; only the last time each member opened it is kept.
create table notification_seen (
    member_id  uuid         primary key references member (id) on delete cascade,
    seen_at    timestamptz  not null
);
