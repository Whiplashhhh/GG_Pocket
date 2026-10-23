package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.SectorView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;
import java.util.UUID;

@Schema(name = "Sector")
public record SectorResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = RequiredMode.REQUIRED) int position,
        String description,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Sorted by number")
                List<CordResponse> cords) {

    static SectorResponse from(SectorView view) {
        return new SectorResponse(
                view.id(),
                view.name(),
                view.position(),
                view.description(),
                view.cords().stream().map(CordResponse::from).toList());
    }
}
