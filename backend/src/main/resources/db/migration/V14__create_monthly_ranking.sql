-- The monthly rankings (spec §8.7, §9.2). The current month, and the previous one until the 8th, are
-- computed live from the sessions. On the 8th at 04:00 (Europe/Paris) a scheduled job freezes the
-- previous month: one closure row per month (which makes the job idempotent), and its ranks.
-- A deleted account hands its frozen ranks over to "Ancien·ne membre", like its sessions: no
-- foreign-key cascade, the account deletion moves them first.
create table monthly_ranking_closure (
    month      date          primary key check (month = date_trunc('month', month)::date),
    closed_at  timestamptz   not null
);

create table monthly_ranking_snapshot (
    id          uuid          primary key,
    month       date          not null references monthly_ranking_closure (month),
    kind        varchar(16)   not null check (kind in ('SESSIONS', 'ASCENTS')),
    rank        integer       not null check (rank >= 1),
    member_id   uuid          not null references member (id),
    value       integer       not null check (value >= 1),
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null
);

create index monthly_ranking_snapshot_month_idx on monthly_ranking_snapshot (month, kind, rank);
create index monthly_ranking_snapshot_member_idx on monthly_ranking_snapshot (member_id);
