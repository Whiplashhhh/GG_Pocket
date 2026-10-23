package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.RegistrationCodeAdminService.CodeSettings;
import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(name = "RegistrationCodeSettings", description = "Absent expiry or maximum: no limit.")
public record RegistrationCodeRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "Affiche de la salle, saison 2026")
                @NotBlank
                @PlainText
                @Size(max = 80)
                String label,
        Instant expiresAt,
        @Min(1) @Max(10_000) Integer maxUses,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active) {

    CodeSettings toSettings() {
        return new CodeSettings(label, expiresAt, maxUses, active);
    }
}
