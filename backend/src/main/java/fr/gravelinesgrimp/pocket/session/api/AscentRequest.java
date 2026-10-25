package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.domain.AscentDraft;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "AscentRequest", description = "A clean ascent is topped, with 0 falls.")
public record AscentRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull UUID routeId,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull ClimbMode mode,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean topped,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean clean,
        @Schema(requiredMode = RequiredMode.REQUIRED) @Min(0) @Max(AscentDraft.MAX_FALLS) int falls,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "First try of my life")
                boolean firstTry,
        @Schema(description = "Out of 10") @Min(1) @Max(10) Integer feeling) {

    AscentDraft toDraft() {
        return new AscentDraft(routeId, mode, topped, clean, falls, firstTry, feeling);
    }
}
