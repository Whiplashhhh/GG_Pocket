package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.api.MediaUrls;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStyle;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "RouteSummary", description = "A route as listed in the topo, with my own marks.")
public record RouteSummaryResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = RequiredMode.REQUIRED) String grade,
        @Schema(requiredMode = RequiredMode.REQUIRED) int gradeRank,
        @Schema(requiredMode = RequiredMode.REQUIRED) int cordNumber,
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID sectorId,
        @Schema(requiredMode = RequiredMode.REQUIRED) String sectorName,
        @Schema(requiredMode = RequiredMode.REQUIRED) int sectorPosition,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "One or two hold colours")
                List<RouteHoldResponse> holds,
        @Schema(requiredMode = RequiredMode.REQUIRED) RouteStatus status,
        @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate openedOn,
        LocalDate plannedDismountOn,
        Instant dismountedAt,
        String photoUrl,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<RouteStyle> styles,
        @Schema(requiredMode = RequiredMode.REQUIRED) int favoriteCount,
        @Schema(requiredMode = RequiredMode.REQUIRED) int commentCount,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Whether it is one of my favourites")
                boolean favorite,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Modes in which I can climb it")
                List<ClimbMode> checks) {

    static RouteSummaryResponse from(RouteSummaryView view) {
        return new RouteSummaryResponse(
                view.id(),
                view.name(),
                view.grade(),
                view.gradeRank(),
                view.cordNumber(),
                view.sectorId(),
                view.sectorName(),
                view.sectorPosition(),
                view.holds().stream().map(RouteHoldResponse::from).toList(),
                view.status(),
                view.openedOn(),
                view.plannedDismountOn(),
                view.dismountedAt(),
                MediaUrls.of(view.photoId()),
                view.styles(),
                view.favoriteCount(),
                view.commentCount(),
                view.favorite(),
                view.checks());
    }
}
