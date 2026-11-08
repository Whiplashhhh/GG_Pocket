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
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "False while the board has not validated the account")
                boolean validated,
        @Schema(
                        description =
                                "Licensed at the club, as declared at sign-up; absent for accounts"
                                        + " that never went through it")
                Boolean declaredLicensed,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "Under 18, for the board and the admin only; never the birth date"
                                        + " (D-119)")
                boolean minor,
        String avatarUrl,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant registeredAt,
        @Schema(description = "Roughly, to the hour; absent if never seen since registration")
                Instant lastSeenAt,
        @Schema(
                        description =
                                "When the account was put to sleep after a year without a visit"
                                        + " (D-119); absent if active or disabled by the board")
                Instant dormantSince,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Sessions logged, ever")
                long sessionCount) {

    static MemberSummaryResponse from(MemberSummaryView view) {
        return new MemberSummaryResponse(
                view.id(),
                view.nickname(),
                view.firstName(),
                view.lastName(),
                view.role(),
                view.status(),
                view.validated(),
                view.declaredLicensed(),
                view.minor(),
                MediaUrls.of(view.avatarId()),
                view.createdAt(),
                view.lastSeenAt(),
                view.dormantSince(),
                view.sessionCount());
    }
}
