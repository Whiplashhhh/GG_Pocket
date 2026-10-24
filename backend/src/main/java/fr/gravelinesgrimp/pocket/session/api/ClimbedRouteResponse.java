package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.domain.ClimbedRoute;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "ClimbedRoute", description = "A route I climbed, and the last day I did.")
public record ClimbedRouteResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID routeId,
        @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate lastClimbedOn) {

    static ClimbedRouteResponse from(ClimbedRoute route) {
        return new ClimbedRouteResponse(route.routeId(), route.lastClimbedOn());
    }
}
