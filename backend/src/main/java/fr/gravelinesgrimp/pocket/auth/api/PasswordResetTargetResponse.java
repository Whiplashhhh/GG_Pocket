package fr.gravelinesgrimp.pocket.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "PasswordResetTarget", description = "Who a valid reset link is for.")
public record PasswordResetTargetResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Their login, as a reminder")
                String nickname) {}
