package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.MemberAdminService.MemberSummaryView;
import fr.gravelinesgrimp.pocket.member.api.MediaUrls;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "MemberSummary", description = "A line of the members table.")
public record MemberSummaryResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) String lastName,
        @Schema(requiredMode = RequiredMode.REQUIRED) Role role,
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberStatus status,
        String avatarUrl,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant registeredAt,
        @Schema(description = "Roughly, to the hour; absent if never seen since registration")
                Instant lastSeenAt) {

    static MemberSummaryResponse from(MemberSummaryView view) {
        return new MemberSummaryResponse(
                view.id(),
                view.nickname(),
                view.firstName(),
                view.lastName(),
                view.role(),
                view.status(),
                MediaUrls.of(view.avatarId()),
                view.createdAt(),
                view.lastSeenAt());
    }
}
