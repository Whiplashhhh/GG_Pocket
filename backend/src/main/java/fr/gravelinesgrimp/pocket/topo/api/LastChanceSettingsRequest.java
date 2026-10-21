package fr.gravelinesgrimp.pocket.topo.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "LastChanceSettings")
public record LastChanceSettingsRequest(
        @Schema(description = "Optional, today or later") LocalDate plannedDismountOn) {}
