package fr.gravelinesgrimp.pocket.stats.api;

import fr.gravelinesgrimp.pocket.stats.domain.Attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "Attendance", description = "Ascents over a period, topped or not.")
public record AttendanceResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) int ascentCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int cleanCount,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Distinct climbers")
                int climberCount,
        @Schema(description = "Rounded share of topped ascents; absent without any ascent")
                Integer successPercent) {

    static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                Math.toIntExact(attendance.ascentCount()),
                Math.toIntExact(attendance.toppedCount()),
                Math.toIntExact(attendance.cleanCount()),
                Math.toIntExact(attendance.climberCount()),
                attendance.successPercent());
    }
}
