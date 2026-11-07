package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "ClosedDaysSettings")
public record ClosedDaysRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "2026-12-21") @NotNull
                LocalDate from,
        @Schema(
                        description = "Last day closed, included; the single day from when absent",
                        example = "2027-01-04")
                LocalDate to,
        @Schema(example = "Vacances de Noël") @PlainText @Size(max = 60) String label) {}
