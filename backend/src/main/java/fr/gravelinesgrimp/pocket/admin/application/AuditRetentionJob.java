package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.admin.infra.AuditEntryRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Keeps the management journal 3 years, as « Tes données » promises (spec §14, security review
 * D-112): every day at 04:30, Gravelines time, the older entries go.
 */
@Component
public class AuditRetentionJob {

    static final int YEARS_KEPT = 3;

    private static final Logger LOG = LoggerFactory.getLogger(AuditRetentionJob.class);

    private final AuditEntryRepository entries;
    private final Clock clock;

    AuditRetentionJob(AuditEntryRepository entries, Clock clock) {
        this.entries = entries;
        this.clock = clock;
    }

    @Scheduled(cron = "0 30 4 * * *", zone = "Europe/Paris")
    @Transactional
    public void run() {
        var removed = entries.deleteOlderThan(limit());
        if (removed > 0) {
            LOG.info("Audit entries older than {} years removed: {}", YEARS_KEPT, removed);
        }
    }

    /** The first instant still kept: the same day and time, 3 years back in Gravelines. */
    public Instant limit() {
        return clock.instant().atZone(ClubTime.ZONE).minusYears(YEARS_KEPT).toInstant();
    }
}
