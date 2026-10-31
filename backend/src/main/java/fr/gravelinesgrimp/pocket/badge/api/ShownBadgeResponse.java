package fr.gravelinesgrimp.pocket.badge.api;

import fr.gravelinesgrimp.pocket.badge.domain.Badge;
import fr.gravelinesgrimp.pocket.badge.domain.ShownBadge;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/** A favourite badge next to a name (D-106). */
@Schema(name = "ShownBadge")
public record ShownBadgeResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) Badge badge,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Tiers earned, or times earned for a repeatable badge")
                long level) {

    public static ShownBadgeResponse from(ShownBadge shown) {
        return new ShownBadgeResponse(shown.badge(), shown.level());
    }
}
