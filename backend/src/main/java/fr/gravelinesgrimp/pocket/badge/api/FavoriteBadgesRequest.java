package fr.gravelinesgrimp.pocket.badge.api;

import fr.gravelinesgrimp.pocket.badge.domain.Badge;
import fr.gravelinesgrimp.pocket.badge.domain.FavoriteBadge;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(name = "FavoriteBadgesRequest")
record FavoriteBadgesRequest(
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "In display order, earned ones, empty to show none")
                @NotNull
                @Size(max = FavoriteBadge.MAX)
                List<@NotNull Badge> badges) {}
