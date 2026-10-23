package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.RouteDetailView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "RouteDetail")
public record RouteDetailResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID gradeId,
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID cordId,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<MemberRefResponse> setters,
        @Schema(description = "Setter without an account") String setterNote,
        String advice,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt) {

    static RouteDetailResponse from(RouteDetailView view) {
        return new RouteDetailResponse(
                RouteSummaryResponse.from(view.route()),
                view.gradeId(),
                view.cordId(),
                view.setters().stream().map(MemberRefResponse::from).toList(),
                view.setterNote(),
                view.advice(),
                view.createdAt());
    }
}
