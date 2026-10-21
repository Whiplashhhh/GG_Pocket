package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.HoldColorSettings;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "HoldColorSettings")
public record HoldColorSettingsRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(min = 1, max = 30)
                String label,
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "#F5C518")
                @NotNull
                @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
                String hex,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean multicolor,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active) {

    HoldColorSettings toSettings() {
        return new HoldColorSettings(label, hex, multicolor, active);
    }
}
