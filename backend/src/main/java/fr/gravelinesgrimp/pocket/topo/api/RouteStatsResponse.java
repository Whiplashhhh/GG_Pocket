package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.RouteStatsView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "RouteStats", description = "Ascents of the route by everyone.")
public record RouteStatsResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) int ascentCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int climberCount,
        @Schema(description = "Rounded share of ascents in lead; absent without any ascent")
                Integer leadPercent) {

    static RouteStatsResponse from(RouteStatsView view) {
        return new RouteStatsResponse(
                view.ascentCount(), view.toppedCount(), view.climberCount(), view.leadPercent());
    }
}
