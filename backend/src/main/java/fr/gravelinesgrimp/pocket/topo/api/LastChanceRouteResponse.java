package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.LastChanceService.LastChanceRouteView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "LastChanceRoute")
public record LastChanceRouteResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
        @Schema(requiredMode = RequiredMode.REQUIRED) int acknowledgedCount) {

    static LastChanceRouteResponse from(LastChanceRouteView view) {
        return new LastChanceRouteResponse(
                RouteSummaryResponse.from(view.route()), view.acknowledgedCount());
    }
}
