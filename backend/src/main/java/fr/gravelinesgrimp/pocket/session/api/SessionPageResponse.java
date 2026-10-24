package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.SessionService.SessionPage;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(name = "SessionPage", description = "nextCursor is absent on the last page.")
public record SessionPageResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<SessionResponse> items,
        String nextCursor) {

    static SessionPageResponse from(SessionPage page) {
        return new SessionPageResponse(
                page.sessions().stream().map(SessionResponse::from).toList(), page.nextCursor());
    }
}
