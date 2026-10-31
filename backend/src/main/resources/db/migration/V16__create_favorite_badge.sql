-- The badges a climber shows next to their name, in the feed and on their profile (D-106): up to
-- three, in the order chosen. A badge lost since (its sessions changed) stays stored but is not
-- shown until earned again. An account deletion drops them with the member.
create table favorite_badge (
    id          uuid          primary key,
    member_id   uuid          not null references member (id) on delete cascade,
    badge       varchar(32)   not null,
    slot        integer       not null,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null,
    unique (member_id, badge)
);
