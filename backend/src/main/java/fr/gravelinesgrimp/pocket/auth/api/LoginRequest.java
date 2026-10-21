package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 100)
                String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(max = 200) String password,
        @Schema(description = "\"Rester connecté\": 180 days instead of the browser session")
                boolean rememberMe) {}
