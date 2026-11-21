-- The feeds show the latest posted sessions first (club feedback, D-131): a session logged days
-- after the climb still shows on top.
create index climbing_session_posted_idx on climbing_session (created_at desc, id desc);
