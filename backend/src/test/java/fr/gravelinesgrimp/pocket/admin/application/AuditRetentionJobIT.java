package fr.gravelinesgrimp.pocket.admin.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** « Tes données » promises a journal kept 3 years (D-112). */
@IntegrationTest
class AuditRetentionJobIT {

    @Autowired private AuditRetentionJob job;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;

    @Test
    void entriesOlderThanThreeYearsGoTheOthersStay() {
        var old = entry(job.limit().minus(Duration.ofDays(1)));
        var kept = entry(job.limit().plus(Duration.ofDays(1)));

        job.run();

        assertThat(exists(old)).isFalse();
        assertThat(exists(kept)).isTrue();
        assertThat(job.limit()).isBefore(clock.instant().minus(Duration.ofDays(3 * 365)));
    }

    private UUID entry(Instant at) {
        var id = UUID.randomUUID();
        jdbc.update(
                "insert into audit_entry (id, occurred_at, action, target_type, target_label)"
                        + " values (?, ?, 'MEMBER_DELETED', 'MEMBER', 'quelqu''un')",
                id,
                Timestamp.from(at));
        return id;
    }

    private boolean exists(UUID id) {
        return jdbc.queryForObject("select count(*) from audit_entry where id = ?", Long.class, id)
                == 1;
    }
}
