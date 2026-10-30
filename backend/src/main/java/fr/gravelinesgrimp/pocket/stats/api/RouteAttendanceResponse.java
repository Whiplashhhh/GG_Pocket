package fr.gravelinesgrimp.pocket.stats.api;

import fr.gravelinesgrimp.pocket.stats.application.RouteAttendanceView;
import fr.gravelinesgrimp.pocket.topo.api.RouteSummaryResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "RouteAttendance", description = "A route and its ascents over a period.")
public record RouteAttendanceResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
        @Schema(requiredMode = RequiredMode.REQUIRED) AttendanceResponse attendance) {

    static RouteAttendanceResponse from(RouteAttendanceView view) {
        return new RouteAttendanceResponse(
                RouteSummaryResponse.from(view.route()),
                AttendanceResponse.from(view.attendance()));
    }
}
