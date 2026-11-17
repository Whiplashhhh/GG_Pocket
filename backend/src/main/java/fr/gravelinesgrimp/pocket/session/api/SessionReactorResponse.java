package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.SessionReactionService.ReactorView;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "SessionReactor")
public record SessionReactorResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse member,
        @Schema(requiredMode = RequiredMode.REQUIRED) ReactionKind kind) {

    static SessionReactorResponse from(ReactorView view) {
        return new SessionReactorResponse(MemberRefResponse.from(view.member()), view.kind());
    }
}
