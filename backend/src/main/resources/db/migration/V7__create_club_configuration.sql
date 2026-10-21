-- The layout of the gym, set by the admin (spec §7): sectors, the cords that hang in them, the hold
-- colours that identify routes, and the fixed list of grades. Nothing here is ever deleted (routes
-- and their history point to it): a cord or a colour is deactivated instead.
-- The seed below is the real club configuration, identical in dev and production.
create table sector (
    id           uuid         primary key,
    name         varchar(60)  not null,
    position     integer      not null,
    description  varchar(160),
    created_at   timestamptz  not null,
    updated_at   timestamptz  not null,
    version      bigint       not null
);

-- "Grand toit" and "grand toit" are the same sector.
create unique index sector_name_key on sector (lower(name));

-- A cord keeps its number for good; moving it to another sector moves its routes with it (the
-- sector shown for a route is the one of its cord).
create table cord (
    id          uuid         primary key,
    number      integer      not null unique check (number between 1 and 99),
    sector_id   uuid         not null references sector (id),
    active      boolean      not null default true,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null
);

create index cord_sector_idx on cord (sector_id);

-- Hex stored in upper case. The multicolour hold has a neutral grey hex: the app draws a pattern.
create table hold_color (
    id          uuid         primary key,
    label       varchar(30)  not null,
    hex         varchar(7)   not null check (hex ~ '^#[0-9A-F]{6}$'),
    multicolor  boolean      not null default false,
    active      boolean      not null default true,
    position    integer      not null,
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null
);

create unique index hold_color_label_key on hold_color (lower(label));

-- French grades, read-only in the app (DECISIONS D-074). The rank sorts and compares them.
create table grade (
    id          uuid         primary key,
    code        varchar(4)   not null unique,
    rank        integer      not null unique check (rank > 0),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null
);

insert into sector (id, name, position, created_at, updated_at, version)
select gen_random_uuid(), seed.name, seed.position, now(), now(), 0
from (values
    ('Neutre 8 m', 1),
    ('Neutre 12 m', 2),
    ('Positif', 3),
    ('Négatif et ses 2 dièdres', 4),
    ('Grand dévers', 5),
    ('Cheminée', 6),
    ('Grand toit', 7),
    ('Sous toit', 8),
    ('Petit dévers', 9),
    ('Petit toit', 10)
) as seed (name, position);

insert into cord (id, number, sector_id, active, created_at, updated_at, version)
select gen_random_uuid(), numbers.n, sector.id, true, now(), now(), 0
from (values
    ('Neutre 8 m', 1, 3),
    ('Neutre 12 m', 4, 6),
    ('Positif', 7, 9),
    ('Négatif et ses 2 dièdres', 10, 14),
    ('Grand dévers', 15, 17),
    ('Cheminée', 18, 19),
    ('Grand toit', 20, 21),
    ('Sous toit', 22, 24),
    ('Petit dévers', 25, 27),
    ('Petit toit', 28, 29)
) as seed (sector_name, first_number, last_number)
join sector on sector.name = seed.sector_name
cross join lateral generate_series(seed.first_number, seed.last_number) as numbers (n);

-- Values of DESIGN.md §2 "Couleurs de prises".
insert into hold_color (id, label, hex, multicolor, active, position, created_at, updated_at, version)
select gen_random_uuid(), seed.label, seed.hex, seed.multicolor, true, seed.position, now(), now(), 0
from (values
    ('Jaune', '#F5C518', false, 1),
    ('Orange', '#F28C28', false, 2),
    ('Rouge', '#D7263D', false, 3),
    ('Rose', '#F06EAA', false, 4),
    ('Violet', '#7B4FB8', false, 5),
    ('Bleu', '#1F5FBF', false, 6),
    ('Bleu clair', '#5BC0EB', false, 7),
    ('Vert', '#2E9E44', false, 8),
    ('Vert clair', '#9BD34A', false, 9),
    ('Noir', '#1A1A1A', false, 10),
    ('Blanc', '#F7F7F2', false, 11),
    ('Gris', '#9A9A9A', false, 12),
    ('Marron', '#7A4B2A', false, 13),
    ('Multicolore', '#808080', true, 14)
) as seed (label, hex, multicolor, position);

insert into grade (id, code, rank, created_at, updated_at, version)
select gen_random_uuid(), seed.code, seed.rank, now(), now(), 0
from (values
    ('4a', 1), ('4a+', 2), ('4b', 3), ('4b+', 4), ('4c', 5), ('4c+', 6),
    ('5a', 7), ('5a+', 8), ('5b', 9), ('5b+', 10), ('5c', 11), ('5c+', 12),
    ('6a', 13), ('6a+', 14), ('6b', 15), ('6b+', 16), ('6c', 17), ('6c+', 18),
    ('7a', 19), ('7a+', 20), ('7b', 21), ('7b+', 22), ('7c', 23)
) as seed (code, rank);
