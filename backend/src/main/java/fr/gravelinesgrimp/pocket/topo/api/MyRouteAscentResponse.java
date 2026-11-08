package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.MyRouteAscentView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "MyRouteAscent", description = "One of my ascents of the route.")
public record MyRouteAscentResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID sessionId,
        @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate climbedOn,
        @Schema(requiredMode = RequiredMode.REQUIRED) ClimbMode mode,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean topped,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Not topped, but past the half of the route")
                boolean pastHalf,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean clean,
        @Schema(requiredMode = RequiredMode.REQUIRED) int falls,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean firstTry) {

    static MyRouteAscentResponse from(MyRouteAscentView view) {
        return new MyRouteAscentResponse(
                view.sessionId(),
                view.climbedOn(),
                view.mode(),
                view.topped(),
                view.pastHalf(),
                view.clean(),
                view.falls(),
                view.firstTry());
    }
}
