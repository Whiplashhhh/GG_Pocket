package fr.gravelinesgrimp.pocket.topo.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "FavoriteRequest")
public record FavoriteRequest(
        @Schema(description = "One of my active favourites to give up for this one")
                UUID replaceRouteId) {}
