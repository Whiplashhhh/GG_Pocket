package fr.gravelinesgrimp.pocket.club.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(name = "SectorOrder")
public record SectorOrderRequest(
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Every sector, in the new order")
                @NotNull
                List<@NotNull UUID> sectorIds) {}
