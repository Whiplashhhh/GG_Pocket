-- Members of the club (the spec's "User"; "user" is a reserved word in PostgreSQL) and the files
-- they upload. Identity only: the ways to sign in (password today, passkeys later) are columns or
-- tables of their own, so a new one can be added without reshaping this table.
create table member (
    id                   uuid         primary key,
    nickname             varchar(20)  not null,
    first_name           varchar(50)  not null,
    last_name            varchar(50)  not null,
    email                varchar(254),
    password_hash        varchar(100) not null,
    role                 varchar(16)  not null check (role in ('CLIMBER', 'SETTER', 'BOARD', 'ADMIN')),
    status               varchar(16)  not null check (status in ('ACTIVE', 'DISABLED')),
    avatar_id            uuid,
    birth_date           date,
    show_age             boolean      not null default false,
    club_arrival_year    smallint,
    climbing_since_year  smallint,
    bio                  varchar(160),
    appears_in_rankings  boolean      not null default true,
    last_seen_version    varchar(32),
    privacy_accepted_at  timestamptz  not null,
    parental_consent_at  timestamptz,
    last_seen_at         timestamptz,
    created_at           timestamptz  not null,
    updated_at           timestamptz  not null,
    version              bigint       not null
);

-- The nickname is the login: unique whatever the case ("Marie" and "marie" are the same person).
create unique index member_nickname_key on member (lower(nickname));
create unique index member_email_key on member (lower(email)) where email is not null;
create index member_role_status_idx on member (role, status);

create table media_file (
    id            uuid         primary key,
    owner_id      uuid         not null references member (id) on delete cascade,
    usage         varchar(16)  not null check (usage in ('AVATAR')),
    content_type  varchar(64)  not null,
    width         integer      not null,
    height        integer      not null,
    size_bytes    integer      not null,
    storage_key   varchar(200) not null unique,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null,
    version       bigint       not null
);

create index media_file_owner_idx on media_file (owner_id);

alter table member
    add constraint member_avatar_fk foreign key (avatar_id) references media_file (id) on delete set null;
