package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.SessionCommentService.SessionCommentView;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "SessionComment")
public record SessionCommentResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse author,
        @Schema(requiredMode = RequiredMode.REQUIRED) String body,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Whether I may delete it")
                boolean deletable) {

    static SessionCommentResponse from(SessionCommentView view) {
        return new SessionCommentResponse(
                view.id(),
                MemberRefResponse.from(view.author()),
                view.body(),
                view.createdAt(),
                view.deletable());
    }
}
