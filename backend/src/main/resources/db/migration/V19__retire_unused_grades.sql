-- The club drops eleven grades (DECISIONS D-115): no modifier below 5b, no "-" below 6b.
-- The rows stay (D-074) but are marked retired: the app no longer offers them, and every route
-- that had one moves to the grade without its modifier (4a+ -> 4a, 6a- -> 6a). To-dos, ascents
-- and badges follow their route.
alter table grade add column retired boolean not null default false;

update grade
set retired = true
where code in ('4a-', '4a+', '4b-', '4b+', '4c-', '4c+', '5a-', '5a+', '5b-', '5c-', '6a-');

update route
set grade_id = base.id
from grade retired_grade, grade base
where route.grade_id = retired_grade.id
  and retired_grade.retired
  and base.code = left(retired_grade.code, 2);
