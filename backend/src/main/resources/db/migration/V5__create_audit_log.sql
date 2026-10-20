-- Who did what, to whom, when, with the value before and after. Append-only.
-- Names are copied at write time so an entry stays readable after a member changes nickname.
create table audit_entry (
    id              uuid         primary key,
    occurred_at     timestamptz  not null,
    actor_id        uuid         references member (id) on delete set null,
    actor_nickname  varchar(20),
    action          varchar(40)  not null,
    target_type     varchar(30)  not null,
    target_id       uuid,
    target_label    varchar(100) not null,
    before_value    varchar(200),
    after_value     varchar(200)
);

create index audit_entry_occurred_idx on audit_entry (occurred_at desc, id desc);
