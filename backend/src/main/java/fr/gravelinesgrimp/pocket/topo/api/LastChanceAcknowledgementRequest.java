package fr.gravelinesgrimp.pocket.topo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(name = "LastChanceAcknowledgement")
public record LastChanceAcknowledgementRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(min = 1, max = 50)
                List<@NotNull UUID> routeIds) {}
