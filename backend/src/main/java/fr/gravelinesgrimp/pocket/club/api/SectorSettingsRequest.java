package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.SectorSettings;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SectorSettings")
public record SectorSettingsRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(min = 1, max = 60)
                String name,
        @Size(max = 160) String description) {

    SectorSettings toSettings() {
        return new SectorSettings(name, description);
    }
}
