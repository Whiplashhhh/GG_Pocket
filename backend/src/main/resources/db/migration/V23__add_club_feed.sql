-- The club feed, both ways (D-120): a third visibility, "Tout le club", read by the members who
-- take part in the club feed, and the member's own choice to take part (off by default). Who reads
-- what is decided when reading: a member who leaves takes their sessions out of the club feed at
-- once, without rewriting them.
alter table climbing_session drop constraint climbing_session_visibility_check;
alter table climbing_session
    add constraint climbing_session_visibility_check
        check (visibility in ('FRIENDS', 'PRIVATE', 'CLUB'));

alter table member add column in_club_feed boolean not null default false;

-- The club feed reads the latest "Tout le club" sessions of everyone.
create index climbing_session_club_day_idx
    on climbing_session (climbed_on desc, created_at desc, id desc)
    where visibility = 'CLUB';
