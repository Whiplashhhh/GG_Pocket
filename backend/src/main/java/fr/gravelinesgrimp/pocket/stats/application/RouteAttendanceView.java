package fr.gravelinesgrimp.pocket.stats.application;

import fr.gravelinesgrimp.pocket.stats.domain.Attendance;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;

/** A route and its ascents over a period. */
public record RouteAttendanceView(RouteSummaryView route, Attendance attendance) {}
