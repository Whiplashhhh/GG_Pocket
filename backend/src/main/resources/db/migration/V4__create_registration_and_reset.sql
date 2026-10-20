-- Club codes that open registration, and single-use password reset links.
create table registration_code (
    id            uuid         primary key,
    code          varchar(16)  not null unique,
    label         varchar(80)  not null,
    granted_role  varchar(16)  not null check (granted_role in ('CLIMBER', 'ADMIN')),
    expires_at    timestamptz,
    max_uses      integer      check (max_uses > 0),
    use_count     integer      not null default 0 check (use_count >= 0),
    active        boolean      not null,
    created_by    uuid         references member (id) on delete set null,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null,
    version       bigint       not null
);

-- Only a hash of the token is stored: a database leak does not leak usable links.
create table password_reset_token (
    id          uuid         primary key,
    member_id   uuid         not null references member (id) on delete cascade,
    token_hash  varchar(64)  not null unique,
    expires_at  timestamptz  not null,
    used_at     timestamptz,
    created_by  uuid         references member (id) on delete set null,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null
);

create index password_reset_token_member_idx on password_reset_token (member_id);
