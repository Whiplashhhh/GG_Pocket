package fr.gravelinesgrimp.pocket.session.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The streak under the calendar (spec §8.6): ISO weeks in a row with at least {@link
 * #MIN_DAYS_PER_WEEK} climbing days, counted back from this week. This week only counts once it has
 * its days; until then it does not break the streak either.
 *
 * @param currentWeekDays climbing days so far this week
 */
public record Streak(int weeks, int minDaysPerWeek, int currentWeekDays) {

    public static final int MIN_DAYS_PER_WEEK = 2;

    /**
     * @param climbingDays days with at least one session (duplicates are fine)
     */
    public static Streak of(Collection<LocalDate> climbingDays, LocalDate today) {
        Map<LocalDate, Long> daysPerWeek =
                climbingDays.stream()
                        .distinct()
                        .filter(day -> !day.isAfter(today))
                        .collect(Collectors.groupingBy(Streak::mondayOf, Collectors.counting()));
        Function<LocalDate, Long> daysIn = monday -> daysPerWeek.getOrDefault(monday, 0L);
        var week = mondayOf(today);
        var currentWeekDays = daysIn.apply(week).intValue();
        if (currentWeekDays < MIN_DAYS_PER_WEEK) {
            week = week.minusWeeks(1);
        }
        var weeks = 0;
        while (daysIn.apply(week) >= MIN_DAYS_PER_WEEK) {
            weeks++;
            week = week.minusWeeks(1);
        }
        return new Streak(weeks, MIN_DAYS_PER_WEEK, currentWeekDays);
    }

    private static LocalDate mondayOf(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
