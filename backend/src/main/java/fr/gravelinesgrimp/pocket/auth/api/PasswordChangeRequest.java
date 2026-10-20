package fr.gravelinesgrimp.pocket.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "PasswordChange")
public record PasswordChangeRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(max = 200)
                String currentPassword,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(max = 200)
                String newPassword) {}
