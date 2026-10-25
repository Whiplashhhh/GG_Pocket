package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "SessionRequest")
public record SessionRequest(
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "Drawn by the app once per new session: sending it again gives"
                                        + " back the session already saved (ignored on update)")
                @NotNull
                UUID clientRef,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Today or up to 7 days back")
                @NotNull
                LocalDate climbedOn,
        @Schema(description = "Out of 10") @Min(1) @Max(10) Integer feeling,
        @PlainText @Size(max = SessionDraft.MAX_COMMENT_LENGTH) String comment,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull SessionVisibility visibility,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "In the order climbed")
                @NotNull
                @Size(min = 1, max = SessionDraft.MAX_ASCENTS)
                List<@NotNull @Valid AscentRequest> ascents) {

    SessionDraft toDraft() {
        return new SessionDraft(
                climbedOn,
                feeling,
                comment,
                visibility,
                ascents.stream().map(AscentRequest::toDraft).toList());
    }
}
