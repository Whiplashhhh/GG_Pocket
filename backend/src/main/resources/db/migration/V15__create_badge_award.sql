-- The badges earned (spec §7 UserBadge, §9.5): one row per time a badge was earned, written by the
-- badge engine after every change of a session (the stored awards follow the computed ones: added,
-- moved to another session or removed). A tier has an empty key; a repeatable badge is told apart by
-- its week, month, session, day or route. Count, first and last date are read from these rows.
-- An account deletion drops them with the member; a deleted session drops its awards, then the
-- engine gives them again to a later session if the condition still holds.
create table badge_award (
    id          uuid          primary key,
    member_id   uuid          not null references member (id) on delete cascade,
    code        varchar(32)   not null,
    award_key   varchar(64)   not null,
    session_id  uuid          not null references climbing_session (id) on delete cascade,
    earned_on   date          not null,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null,
    unique (member_id, code, award_key)
);

create index badge_award_session_idx on badge_award (session_id);
