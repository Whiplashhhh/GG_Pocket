package fr.gravelinesgrimp.pocket.stats.api;

import fr.gravelinesgrimp.pocket.stats.application.MonthAttendanceView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(name = "MonthAttendance")
public record MonthAttendanceResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "2026-10") String month,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "Every route together: a climber of several routes counts once")
                AttendanceResponse total,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "Routes on the wall during the month, climbed or not, and those"
                                        + " climbed in it; most climbed first")
                List<RouteAttendanceResponse> routes) {

    static MonthAttendanceResponse from(MonthAttendanceView view) {
        return new MonthAttendanceResponse(
                view.month().toString(),
                AttendanceResponse.from(view.total()),
                view.routes().stream().map(RouteAttendanceResponse::from).toList());
    }
}
