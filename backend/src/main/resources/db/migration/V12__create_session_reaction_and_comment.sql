-- Reactions and comments on sessions (spec §7, §8.2). Whoever reads a session may comment on it,
-- and react to it when it is someone else's (one reaction per member, from a fixed set of four,
-- changeable). Deleting a comment only hides it, like the comments on routes: the board still sees
-- what was moderated, and by whom.
-- A session that goes away takes its reactions and comments with it.
create table session_reaction (
    id          uuid          primary key,
    session_id  uuid          not null references climbing_session (id) on delete cascade,
    member_id   uuid          not null references member (id) on delete cascade,
    kind        varchar(16)   not null check (kind in ('FIRE', 'STRONG', 'CLAP', 'GORILLA')),
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null,
    constraint session_reaction_session_member_key unique (session_id, member_id)
);

create index session_reaction_member_idx on session_reaction (member_id);

create table session_comment (
    id          uuid          primary key,
    session_id  uuid          not null references climbing_session (id) on delete cascade,
    author_id   uuid          not null references member (id) on delete cascade,
    body        varchar(500)  not null,
    deleted_at  timestamptz,
    deleted_by  uuid          references member (id) on delete set null,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null
);

create index session_comment_session_idx on session_comment (session_id, created_at);
create index session_comment_author_idx on session_comment (author_id);

-- The moderation list (spec §8.11): the latest comments first, on routes and on sessions.
create index session_comment_latest_idx on session_comment (created_at desc, id desc)
    where deleted_at is null;
create index route_comment_latest_idx on route_comment (created_at desc, id desc)
    where deleted_at is null;
