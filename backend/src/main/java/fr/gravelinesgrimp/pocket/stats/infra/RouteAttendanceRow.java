package fr.gravelinesgrimp.pocket.stats.infra;

import fr.gravelinesgrimp.pocket.stats.domain.Attendance;

import java.util.UUID;

/** The ascents of one route over a period, as counted by the database. */
public record RouteAttendanceRow(
        UUID routeId, long ascentCount, long toppedCount, long cleanCount, long climberCount) {

    public Attendance toAttendance() {
        return new Attendance(ascentCount, toppedCount, cleanCount, climberCount);
    }
}
