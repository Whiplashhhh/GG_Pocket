-- Who held the rope, route by route (an optional club member, never the climber). Naming someone
-- gives them no right on the session; they may take their name back. A deleted account leaves the
-- ascents without a belayer.
alter table ascent add column belayer_id uuid references member (id) on delete set null;

-- "Mes assurages": the sessions where I held the rope.
create index ascent_belayer_idx on ascent (belayer_id, session_id) where belayer_id is not null;
