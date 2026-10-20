package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.RegistrationCodeAdminService.RegistrationCodeView;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "RegistrationCode")
public record RegistrationCodeResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "K7XM-P2QA") String code,
        @Schema(requiredMode = RequiredMode.REQUIRED) String label,
        Instant expiresAt,
        Integer maxUses,
        @Schema(requiredMode = RequiredMode.REQUIRED) int useCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean active,
        @Schema(requiredMode = RequiredMode.REQUIRED) RegistrationCode.State state,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt) {

    static RegistrationCodeResponse from(RegistrationCodeView view) {
        return new RegistrationCodeResponse(
                view.id(),
                view.code(),
                view.label(),
                view.expiresAt(),
                view.maxUses(),
                view.useCount(),
                view.active(),
                view.state(),
                view.createdAt());
    }
}
