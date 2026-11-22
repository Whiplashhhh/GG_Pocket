-- A comment on a session may answer another one of the same session (club feedback, D-128).
-- The comment answered is only hidden when deleted, so the link stays; it goes with an erased account.
alter table session_comment
    add column reply_to_id uuid references session_comment (id) on delete set null;

create index session_comment_reply_to_idx on session_comment (reply_to_id)
    where reply_to_id is not null;
