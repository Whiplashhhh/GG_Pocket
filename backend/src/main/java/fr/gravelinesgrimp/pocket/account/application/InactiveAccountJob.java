package fr.gravelinesgrimp.pocket.account.application;

import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Inactive accounts, as « Tes données » promises (D-119): every day at 03:15, Gravelines time, an
 * account without a visit for a year goes to sleep, one without a visit for two years is deleted
 * like a deletion of one's own. Admins are left alone. One transaction per account: one that fails
 * does not hold back the others, and is tried again the next day.
 */
@Component
public class InactiveAccountJob {

    private static final Logger LOG = LoggerFactory.getLogger(InactiveAccountJob.class);

    private final MemberRepository members;
    private final InactiveAccountService inactiveAccounts;
    private final AccountDeletionService deletions;
    private final Clock clock;

    InactiveAccountJob(
            MemberRepository members,
            InactiveAccountService inactiveAccounts,
            AccountDeletionService deletions,
            Clock clock) {
        this.members = members;
        this.inactiveAccounts = inactiveAccounts;
        this.deletions = deletions;
        this.clock = clock;
    }

    @Scheduled(cron = "0 15 3 * * *", zone = "Europe/Paris")
    public void run() {
        var now = clock.instant();
        var asleep = 0;
        var deleted = 0;
        // A few hundred members at most: read them all, decide here, act one by one.
        for (var member : members.findAllForDirectory()) {
            try {
                switch (member.inactivity(now)) {
                    case PUT_TO_SLEEP -> {
                        if (inactiveAccounts.putToSleepIfInactive(member.id())) {
                            asleep++;
                        }
                    }
                    case DELETE -> {
                        if (deletions.deleteIfInactive(member.id())) {
                            deleted++;
                        }
                    }
                    case KEEP -> {
                        // Seen within the year, or an admin.
                    }
                }
            } catch (RuntimeException e) {
                LOG.warn("Inactive account {} left for tomorrow", member.id(), e);
            }
        }
        if (asleep > 0 || deleted > 0) {
            LOG.info("Inactive accounts: {} put to sleep, {} deleted", asleep, deleted);
        }
    }
}
