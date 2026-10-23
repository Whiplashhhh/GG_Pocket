package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView.HoldView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "RouteHold")
public record RouteHoldResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String label,
        @Schema(requiredMode = RequiredMode.REQUIRED) String hex,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean multicolor) {

    static RouteHoldResponse from(HoldView view) {
        return new RouteHoldResponse(view.id(), view.label(), view.hex(), view.multicolor());
    }
}
