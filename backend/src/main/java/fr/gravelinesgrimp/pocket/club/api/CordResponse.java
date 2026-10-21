package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.CordView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "Cord")
public record CordResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) int number,
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID sectorId,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active) {

    static CordResponse from(CordView view) {
        return new CordResponse(view.id(), view.number(), view.sectorId(), view.active());
    }
}
