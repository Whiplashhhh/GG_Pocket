-- The days the gym is closed (holidays, bank holidays, summer), set by the board (D-116). A closed
-- day is not a free session day: the "Mois complet" badge does not wait for it, and the planning
-- shows it. One row per date; deleting a row reopens the day.
create table closed_day (
    id          uuid         primary key,
    closed_on   date         not null unique,
    label       varchar(60),
    created_at  timestamptz  not null,
    updated_at  timestamptz  not null,
    version     bigint       not null
);
