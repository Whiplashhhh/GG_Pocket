-- One-row table recording which environment (LOCAL, DEV, PROD) this database belongs to.
-- Written at first start-up; the application refuses to start if it is later configured for
-- another environment (see InstanceMarkerGuard). Never updated, hence no updated_at.
create table instance_marker (
    id          smallint    primary key check (id = 1),
    environment varchar(16) not null check (environment in ('LOCAL', 'DEV', 'PROD')),
    created_at  timestamptz not null
);
