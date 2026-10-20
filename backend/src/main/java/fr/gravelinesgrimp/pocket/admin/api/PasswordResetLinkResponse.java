package fr.gravelinesgrimp.pocket.admin.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;

@Schema(
        name = "PasswordResetLink",
        description =
                "The secret of a reset link, shown once. The app builds the link with its own"
                        + " address: /nouveau-mot-de-passe#<token>.")
public record PasswordResetLinkResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) String token,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant expiresAt) {}
