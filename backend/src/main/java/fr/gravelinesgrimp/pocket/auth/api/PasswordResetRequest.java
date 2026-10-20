package fr.gravelinesgrimp.pocket.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "PasswordReset")
public record PasswordResetRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(max = 100) String token,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(max = 200)
                String newPassword) {}
