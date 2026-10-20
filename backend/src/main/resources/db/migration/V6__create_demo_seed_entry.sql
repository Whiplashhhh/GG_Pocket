-- Members created by the demo seed (dev profile only, spec §17). The seed refuses to run as soon as
-- the database holds a member missing from this list: it never touches real accounts.
-- Empty and unused in production, where the seeder does not even exist.
create table demo_seed_entry (
    member_id  uuid        primary key references member (id) on delete cascade,
    created_at timestamptz not null
);
