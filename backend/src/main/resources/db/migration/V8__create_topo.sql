-- The topo (spec §7 and §8.4): routes on the cords, what members say and mark about them.
-- Routes are never deleted: a dismounted route stays readable, with its comments and favourites,
-- in the archive and in the history of the members.
create table route (
    id                   uuid         primary key,
    name                 varchar(60)  not null,
    grade_id             uuid         not null references grade (id),
    cord_id              uuid         not null references cord (id),
    -- One or two hold colours, in the order the setter chose them.
    primary_color_id     uuid         not null references hold_color (id),
    secondary_color_id   uuid         references hold_color (id),
    opened_on            date         not null,
    -- A setter without an account, besides (or instead of) the members in route_setter.
    setter_note          varchar(60),
    advice               varchar(500),
    photo_id             uuid         references media_file (id) on delete set null,
    status               varchar(16)  not null check (status in ('ACTIVE', 'LAST_CHANCE', 'DISMOUNTED')),
    planned_dismount_on  date,
    dismounted_at        timestamptz,
    author_id            uuid         references member (id) on delete set null,
    created_at           timestamptz  not null,
    updated_at           timestamptz  not null,
    version              bigint       not null,
    constraint route_distinct_colors_check
        check (secondary_color_id is null or secondary_color_id <> primary_color_id),
    constraint route_dismounted_at_check check ((status = 'DISMOUNTED') = (dismounted_at is not null)),
    constraint route_planned_dismount_check check (planned_dismount_on is null or status = 'LAST_CHANCE')
);

create index route_status_idx on route (status);
create index route_cord_idx on route (cord_id);

-- Setters with an account (role SETTER and above when they were chosen).
create table route_setter (
    route_id   uuid not null references route (id) on delete cascade,
    member_id  uuid not null references member (id) on delete cascade,
    primary key (route_id, member_id)
);

create index route_setter_member_idx on route_setter (member_id);

create table route_style (
    route_id  uuid        not null references route (id) on delete cascade,
    style     varchar(16) not null check (style in ('CONTINUITY', 'ENDURANCE', 'PHYSICAL', 'TECHNICAL',
        'COMPLEX', 'SLAB', 'OVERHANG', 'CRIMPS', 'JUGS', 'PINCHES', 'SLOPERS', 'DIHEDRAL', 'ROOF', 'REACHY')),
    primary key (route_id, style)
);

-- Deleted comments are kept (deleted_at, deleted_by) for moderation; they are no longer shown.
create table route_comment (
    id          uuid          primary key,
    route_id    uuid          not null references route (id),
    author_id   uuid          not null references member (id) on delete cascade,
    body        varchar(500)  not null,
    deleted_at  timestamptz,
    deleted_by  uuid          references member (id) on delete set null,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null
);

create index route_comment_route_idx on route_comment (route_id, created_at);
create index route_comment_author_idx on route_comment (author_id);

-- Hearts ("coups de cœur"). At most 2 on routes still on the wall, checked by the service under a
-- lock on the member row; the ones on dismounted routes stay as history.
create table favorite (
    id          uuid         primary key,
    member_id   uuid         not null references member (id) on delete cascade,
    route_id    uuid         not null references route (id),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null,
    constraint favorite_member_route_key unique (member_id, route_id)
);

create index favorite_route_idx on favorite (route_id);

-- "Je sais la faire": ticked by hand (MANUAL), or later from a logged ascent (ASCENT, phase 4).
create table route_check (
    id          uuid         primary key,
    member_id   uuid         not null references member (id) on delete cascade,
    route_id    uuid         not null references route (id),
    mode        varchar(16)  not null check (mode in ('TOP_ROPE', 'LEAD')),
    source      varchar(16)  not null check (source in ('MANUAL', 'ASCENT')),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null,
    constraint route_check_member_route_mode_key unique (member_id, route_id, mode)
);

create index route_check_route_idx on route_check (route_id);

-- Who saw the "last chance" pop-up for a route. Cleared when the route leaves last chance, so the
-- pop-up comes back if it is announced again.
create table last_chance_ack (
    id          uuid         primary key,
    member_id   uuid         not null references member (id) on delete cascade,
    route_id    uuid         not null references route (id),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null,
    constraint last_chance_ack_member_route_key unique (member_id, route_id)
);

create index last_chance_ack_route_idx on last_chance_ack (route_id);

-- The favourite route of the profile (spec §7): may point to a dismounted route.
alter table member
    add column favorite_route_id uuid references route (id) on delete set null;

alter table media_file drop constraint media_file_usage_check;
alter table media_file
    add constraint media_file_usage_check check (usage in ('AVATAR', 'ROUTE_PHOTO'));

-- A route photo belongs to the club, not to the setter who sent it: it outlives the account.
-- (Deleting an account must remove its avatar file and row itself: files are not in the database.)
alter table media_file alter column owner_id drop not null;
alter table media_file drop constraint media_file_owner_id_fkey;
alter table media_file
    add constraint media_file_owner_id_fkey
        foreign key (owner_id) references member (id) on delete set null;
