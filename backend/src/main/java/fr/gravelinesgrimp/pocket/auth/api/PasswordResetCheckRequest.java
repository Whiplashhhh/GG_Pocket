package fr.gravelinesgrimp.pocket.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "PasswordResetCheck")
public record PasswordResetCheckRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(max = 100) String token) {}
