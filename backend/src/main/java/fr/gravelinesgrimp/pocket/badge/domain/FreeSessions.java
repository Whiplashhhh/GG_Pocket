package fr.gravelinesgrimp.pocket.badge.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Set;

/**
 * The club's free session days (D-116): every Tuesday, Thursday and Sunday, except the days the
 * board closed the gym. The "Mois complet" badge waits for each of them in its month.
 */
public final class FreeSessions {

    public static final Set<DayOfWeek> WEEKDAYS =
            EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY);

    private FreeSessions() {}

    public static boolean isFreeSessionDay(LocalDate day, Set<LocalDate> closedDays) {
        return WEEKDAYS.contains(day.getDayOfWeek()) && !closedDays.contains(day);
    }

    /** The free session days of the month; 0 when the gym is closed all month. */
    public static int countIn(YearMonth month, Set<LocalDate> closedDays) {
        return (int)
                month.atDay(1)
                        .datesUntil(month.atEndOfMonth().plusDays(1))
                        .filter(day -> isFreeSessionDay(day, closedDays))
                        .count();
    }
}
