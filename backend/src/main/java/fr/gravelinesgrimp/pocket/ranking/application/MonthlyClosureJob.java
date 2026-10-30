package fr.gravelinesgrimp.pocket.ranking.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Runs the monthly closure every day at 04:00, Gravelines time: on the 8th it freezes the previous
 * month (spec §9.2), the other days it catches up a run the server missed.
 */
@Component
class MonthlyClosureJob {

    private static final Logger LOG = LoggerFactory.getLogger(MonthlyClosureJob.class);

    private final RankingClosure closure;
    private final Clock clock;

    MonthlyClosureJob(RankingClosure closure, Clock clock) {
        this.closure = closure;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Paris")
    void run() {
        var closed = closure.closeDueMonths(clock.instant());
        if (!closed.isEmpty()) {
            LOG.info("Rankings frozen for {}", closed);
        }
    }
}
