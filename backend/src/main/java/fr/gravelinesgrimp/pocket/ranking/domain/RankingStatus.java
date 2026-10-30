package fr.gravelinesgrimp.pocket.ranking.domain;

import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/** Where a month's rankings stand (spec §9.2). */
public enum RankingStatus {
    /** The current month: it moves with every session. */
    LIVE,
    /** A past month whose sessions may still be logged, until the 8th at 04:00. */
    PROVISIONAL,
    /** Final: its sessions can no longer change. */
    FROZEN;

    public static RankingStatus of(YearMonth month, Instant now, LocalDate today) {
        if (SessionCalendar.isFrozen(month.atDay(1), now)) {
            return FROZEN;
        }
        return month.equals(YearMonth.from(today)) ? LIVE : PROVISIONAL;
    }
}
