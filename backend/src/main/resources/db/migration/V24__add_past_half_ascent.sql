-- "Plus de la moitié" (DECISIONS D-121): a route not topped but climbed past its half. It never goes
-- with a top, and it counts half a point in the "Voies" ranking of the podium, nothing else.
alter table ascent add column past_half boolean not null default false;
alter table ascent
    add constraint ascent_past_half_check check (not (past_half and topped));

-- The "Voies" ranking now keeps halves (12.5): the frozen scores become decimals with one digit,
-- the existing whole scores unchanged. A member with only half a point is ranked too.
alter table monthly_ranking_snapshot drop constraint monthly_ranking_snapshot_value_check;
alter table monthly_ranking_snapshot alter column value type numeric(7, 1);
alter table monthly_ranking_snapshot
    add constraint monthly_ranking_snapshot_value_check check (value > 0);
