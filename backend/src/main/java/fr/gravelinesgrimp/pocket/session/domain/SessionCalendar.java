package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

/**
 * When a session may be dated, changed or deleted (spec §9.2, §9.3, D-009), in Gravelines time.
 *
 * <ul>
 *   <li>A new session is dated today or up to {@value #DAYS_BACK} days back, never in the future.
 *   <li>A month is frozen from the {@value #FREEZE_DAY}th of the next month at 04:00: its sessions
 *       can no longer be created, changed or deleted (its rankings are final).
 * </ul>
 */
public final class SessionCalendar {

    public static final int DAYS_BACK = 7;
    static final int FREEZE_DAY = 8;
    static final LocalTime FREEZE_TIME = LocalTime.of(4, 0);

    private SessionCalendar() {}

    /** Whether the month of {@code day} is frozen at {@code now}. */
    public static boolean isFrozen(LocalDate day, Instant now) {
        var freezesAt =
                YearMonth.from(day)
                        .plusMonths(1)
                        .atDay(FREEZE_DAY)
                        .atTime(FREEZE_TIME)
                        .atZone(ClubTime.ZONE)
                        .toInstant();
        return !now.isBefore(freezesAt);
    }

    /**
     * Checks the date of a new session, or the new date of a session being changed.
     *
     * @throws BusinessException {@code DATE_IN_THE_FUTURE}, {@code SESSION_TOO_OLD}, {@code
     *     MONTH_FROZEN}
     */
    public static void checkNewDate(LocalDate day, Instant now) {
        var today = LocalDate.ofInstant(now, ClubTime.ZONE);
        if (day.isAfter(today)) {
            throw new BusinessException(ErrorCode.DATE_IN_THE_FUTURE, "Session in the future");
        }
        if (day.isBefore(today.minusDays(DAYS_BACK))) {
            throw new BusinessException(ErrorCode.SESSION_TOO_OLD, "Session too far back");
        }
        checkNotFrozen(day, now);
    }

    /**
     * Checks that a session dated {@code day} may still be changed or deleted.
     *
     * @throws BusinessException {@code MONTH_FROZEN}
     */
    public static void checkNotFrozen(LocalDate day, Instant now) {
        if (isFrozen(day, now)) {
            throw new BusinessException(ErrorCode.MONTH_FROZEN, "Month " + YearMonth.from(day));
        }
    }
}
