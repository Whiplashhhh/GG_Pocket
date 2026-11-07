package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.MonthTotals;
import fr.gravelinesgrimp.pocket.session.domain.Streak;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * One month of my planning: the days I climbed, the month summary, my current streak, and the days
 * the gym is closed (D-116).
 */
public record PlanningView(
        YearMonth month,
        boolean frozen,
        List<Day> days,
        MonthTotals totals,
        Streak streak,
        List<ClosedDay> closedDays) {

    /** A day with at least one session; sessions oldest first. */
    public record Day(LocalDate date, List<SessionView> sessions) {}

    /** A day the gym is closed; the label may be null. */
    public record ClosedDay(LocalDate date, String label) {}
}
