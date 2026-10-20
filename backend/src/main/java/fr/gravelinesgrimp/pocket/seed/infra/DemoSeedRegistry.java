package fr.gravelinesgrimp.pocket.seed.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/** Which members the demo seed created (table {@code demo_seed_entry}). */
@Component
public class DemoSeedRegistry {

    private final JdbcTemplate jdbc;

    public DemoSeedRegistry(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countMembersNotFromTheSeed() {
        Long count =
                jdbc.queryForObject(
                        "select count(*) from member m where not exists"
                                + " (select 1 from demo_seed_entry d where d.member_id = m.id)",
                        Long.class);
        return count == null ? 0 : count;
    }

    public void register(UUID memberId, Instant now) {
        jdbc.update(
                "insert into demo_seed_entry (member_id, created_at) values (?, ?)",
                memberId,
                Timestamp.from(now));
    }
}
