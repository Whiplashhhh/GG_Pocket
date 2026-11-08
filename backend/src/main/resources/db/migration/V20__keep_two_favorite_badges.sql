-- Two favourite badges at most (DECISIONS D-116, was three): a climber who chose three keeps the
-- first two, in the order they chose.
delete from favorite_badge
where id in (
    select id
    from (
        select id, row_number() over (partition by member_id order by slot) as position
        from favorite_badge
    ) ranked
    where ranked.position > 2
);
