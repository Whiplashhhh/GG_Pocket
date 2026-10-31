-- Tourists and account validation (D-107). A new account is a tourist awaiting validation by the
-- board, which makes it a climber (licensed at the club) or keeps it a tourist (occasional visitor).
-- At sign-up the person says whether they are licensed and, if not, who they are to the club: that
-- note only helps the board decide and is erased once it has.
alter table member drop constraint member_role_check;
alter table member
    add constraint member_role_check
        check (role in ('TOURIST', 'CLIMBER', 'SETTER', 'BOARD', 'ADMIN'));

-- Null while awaiting validation. Every account created before this migration counts as validated.
alter table member add column validated_at timestamptz;
update member set validated_at = created_at;

-- What the person declared at sign-up; null for accounts that never went through it.
alter table member add column declared_licensed boolean;
alter table member add column join_note varchar(300);

create index member_awaiting_validation_idx on member (created_at) where validated_at is null;

-- Club codes now open tourist accounts; only the first-start code still grants the admin role.
alter table registration_code drop constraint registration_code_granted_role_check;
update registration_code set granted_role = 'TOURIST' where granted_role = 'CLIMBER';
alter table registration_code
    add constraint registration_code_granted_role_check
        check (granted_role in ('TOURIST', 'ADMIN'));
