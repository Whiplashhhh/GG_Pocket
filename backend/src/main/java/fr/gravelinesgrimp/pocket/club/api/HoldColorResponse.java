package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.HoldColorView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "HoldColor")
public record HoldColorResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String label,
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "#F5C518") String hex,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean multicolor,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active,
        @Schema(requiredMode = RequiredMode.REQUIRED) int position) {

    static HoldColorResponse from(HoldColorView view) {
        return new HoldColorResponse(
                view.id(),
                view.label(),
                view.hex(),
                view.multicolor(),
                view.active(),
                view.position());
    }
}
