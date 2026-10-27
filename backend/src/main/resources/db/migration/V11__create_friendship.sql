-- Friendships (spec §7, §8.2). One row per pair of members, whoever asked: a request is PENDING
-- until the other one accepts it; declining, cancelling or "unfriending" deletes the row. Once
-- accepted the relation is symmetric. Leaving the club (account deletion) deletes the rows.
create table friendship (
    id            uuid          primary key,
    requester_id  uuid          not null references member (id) on delete cascade,
    addressee_id  uuid          not null references member (id) on delete cascade,
    status        varchar(16)   not null check (status in ('PENDING', 'ACCEPTED')),
    accepted_at   timestamptz,
    created_at    timestamptz   not null,
    updated_at    timestamptz   not null,
    version       bigint        not null,
    constraint friendship_not_self check (requester_id <> addressee_id),
    constraint friendship_accepted_at check ((status = 'ACCEPTED') = (accepted_at is not null))
);

-- One row per pair, in either direction.
create unique index friendship_pair_uq
    on friendship (least(requester_id, addressee_id), greatest(requester_id, addressee_id));
create index friendship_requester_idx on friendship (requester_id, status);
create index friendship_addressee_idx on friendship (addressee_id, status);
