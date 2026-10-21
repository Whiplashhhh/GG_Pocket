package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegistrationCodeCheck")
public record RegistrationCodeCheckRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "K7XM-P2QA")
                @NotBlank
                @PlainText
                @Size(max = 32)
                String code) {}
