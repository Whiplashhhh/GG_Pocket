package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.SessionView;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(
        name = "SessionReactions",
        description =
                "One count per reaction, always all four in a fixed order; mine is absent if I gave none.")
public record SessionReactionsResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<ReactionCountItem> counts,
        ReactionKind mine) {

    @Schema(name = "ReactionCount")
    public record ReactionCountItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) ReactionKind kind,
            @Schema(requiredMode = RequiredMode.REQUIRED) int count) {}

    static SessionReactionsResponse from(SessionView.ReactionsView view) {
        return new SessionReactionsResponse(
                view.counts().stream()
                        .map(count -> new ReactionCountItem(count.kind(), count.count()))
                        .toList(),
                view.mine());
    }
}
