package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.CordSettings;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "CordSettings")
public record CordSettingsRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @Min(1) @Max(99) int number,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull UUID sectorId,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active) {

    CordSettings toSettings() {
        return new CordSettings(number, sectorId, active);
    }
}
