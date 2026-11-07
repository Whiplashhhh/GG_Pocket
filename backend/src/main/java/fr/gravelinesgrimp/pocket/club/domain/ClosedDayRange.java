package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * The days from {@code from} to {@code to}, both included, that the board closes or reopens in one
 * go (D-116): a whole summer fits, a typo of ten years does not.
 */
public record ClosedDayRange(LocalDate from, LocalDate to) {

    /** A year and a day: from the 1st of July to the 31st of August is far below. */
    public static final int MAX_DAYS = 366;

    /** The first year of the planning: nothing to close before. */
    public static final int FIRST_YEAR = 2000;

    /** How far ahead the board can close the gym, in years after today's. */
    public static final int YEARS_AHEAD = 2;

    /**
     * @param to null for the single day {@code from}
     * @throws BusinessException {@code VALIDATION_FAILED} for a range backwards, too long, or out
     *     of the years the app knows
     */
    public static ClosedDayRange of(LocalDate from, LocalDate to, LocalDate today) {
        var last = to == null ? from : to;
        if (from == null || last.isBefore(from)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Range ends before it starts");
        }
        if (ChronoUnit.DAYS.between(from, last) >= MAX_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Range too long");
        }
        if (from.getYear() < FIRST_YEAR || last.getYear() > today.getYear() + YEARS_AHEAD) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Range out of years");
        }
        return new ClosedDayRange(from, last);
    }

    public List<LocalDate> days() {
        return from.datesUntil(to.plusDays(1)).toList();
    }

    /** "2026-12-21" for a single day, "2026-12-21 / 2027-01-04" otherwise (audit log). */
    public String describe() {
        return from.equals(to) ? from.toString() : from + " / " + to;
    }
}
