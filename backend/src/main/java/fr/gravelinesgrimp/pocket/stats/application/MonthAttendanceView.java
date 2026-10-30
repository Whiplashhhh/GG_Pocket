package fr.gravelinesgrimp.pocket.stats.application;

import fr.gravelinesgrimp.pocket.stats.domain.Attendance;

import java.time.YearMonth;
import java.util.List;

/**
 * The attendance of a month: the routes on the wall during the month (climbed or not) and those
 * climbed in it, with the month's total.
 */
public record MonthAttendanceView(
        YearMonth month, Attendance total, List<RouteAttendanceView> routes) {}
