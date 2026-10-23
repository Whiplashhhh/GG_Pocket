package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.application.RouteCommentService.RouteCommentView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "RouteComment")
public record RouteCommentResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse author,
        @Schema(requiredMode = RequiredMode.REQUIRED) String body,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Whether I may delete it")
                boolean deletable) {

    static RouteCommentResponse from(RouteCommentView view) {
        return new RouteCommentResponse(
                view.id(),
                MemberRefResponse.from(view.author()),
                view.body(),
                view.createdAt(),
                view.deletable());
    }
}
