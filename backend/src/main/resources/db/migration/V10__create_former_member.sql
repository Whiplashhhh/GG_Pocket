-- "Ancien·ne membre" (spec §14, DECISIONS D-096): when an account is deleted, its sessions and the
-- routes it set are handed over to this single anonymous member, so the routes' statistics stay
-- true. It cannot sign in: its nickname is not a valid one (no lookup at login), it is disabled,
-- and its password hash is the bcrypt of a random value nobody kept.
insert into member (id, nickname, first_name, last_name, email, password_hash, role, status,
                    appears_in_rankings, privacy_accepted_at, created_at, updated_at, version)
values ('00000000-0000-0000-0000-00000000a5c1', 'Ancien·ne membre', 'Ancien·ne', 'membre', null,
        '{bcrypt}$2b$10$c1bp/.8x5X4L7iLU5exY6ujhTCSni0aaLenpsdlT0HLWzLNb/mky.', 'CLIMBER', 'DISABLED',
        false, now(), now(), now(), 0);
