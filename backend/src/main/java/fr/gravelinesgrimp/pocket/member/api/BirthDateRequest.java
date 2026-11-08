package fr.gravelinesgrimp.pocket.member.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(
        name = "BirthDateRequest",
        description = "The birth date an older account never gave, asked at sign-in (D-119).")
public record BirthDateRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Past LocalDate birthDate,
        @Schema(description = "Declares that a parent agreed; required under 15")
                boolean parentalConsent) {}
