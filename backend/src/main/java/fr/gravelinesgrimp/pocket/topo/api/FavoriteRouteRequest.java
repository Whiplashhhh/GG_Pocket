package fr.gravelinesgrimp.pocket.topo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "FavoriteRouteRequest")
public record FavoriteRouteRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull UUID routeId) {}
