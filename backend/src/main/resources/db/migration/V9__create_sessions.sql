-- Sessions at the gym and their ascents (spec §7, §8.3, §9). "session" is left to Spring Session:
-- the table is climbing_session.
create table climbing_session (
    id          uuid          primary key,
    member_id   uuid          not null references member (id) on delete cascade,
    -- The day in Gravelines (Europe/Paris), never in the future, at most 7 days back when logged.
    climbed_on  date          not null,
    feeling     integer       check (feeling between 1 and 10),
    comment     varchar(500),
    photo_id    uuid          references media_file (id) on delete set null,
    visibility  varchar(16)   not null check (visibility in ('FRIENDS', 'PRIVATE')),
    -- Drawn by the app before the first try: sending the same session again (offline queue) gives
    -- back the one already saved instead of a copy.
    client_ref  uuid          not null,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null,
    constraint climbing_session_member_client_ref_key unique (member_id, client_ref)
);

create index climbing_session_member_day_idx
    on climbing_session (member_id, climbed_on desc, created_at desc);
create index climbing_session_photo_idx on climbing_session (photo_id);

-- One try of a route during a session; the same route may come back in a session.
create table ascent (
    id          uuid         primary key,
    session_id  uuid         not null references climbing_session (id) on delete cascade,
    route_id    uuid         not null references route (id),
    position    integer      not null check (position >= 0),
    mode        varchar(16)  not null check (mode in ('TOP_ROPE', 'LEAD')),
    topped      boolean      not null,
    -- Clean ("enchaînée"): topped without a fall nor a rest on the rope.
    clean       boolean      not null,
    falls       integer      not null check (falls between 0 and 99),
    first_try   boolean      not null,
    feeling     integer      check (feeling between 1 and 10),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null,
    constraint ascent_session_position_key unique (session_id, position),
    constraint ascent_clean_check check (not clean or (topped and falls = 0))
);

create index ascent_route_idx on ascent (route_id);

alter table media_file drop constraint media_file_usage_check;
alter table media_file
    add constraint media_file_usage_check
        check (usage in ('AVATAR', 'ROUTE_PHOTO', 'SESSION_PHOTO'));
