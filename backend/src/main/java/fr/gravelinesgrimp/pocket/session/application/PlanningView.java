package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.MonthTotals;
import fr.gravelinesgrimp.pocket.session.domain.Streak;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** One month of my planning: the days I climbed, the month summary and my current streak. */
public record PlanningView(
        YearMonth month, boolean frozen, List<Day> days, MonthTotals totals, Streak streak) {

    /** A day with at least one session; sessions oldest first. */
    public record Day(LocalDate date, List<SessionView> sessions) {}
}
