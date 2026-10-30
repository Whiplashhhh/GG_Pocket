package fr.gravelinesgrimp.pocket.member.api;

import fr.gravelinesgrimp.pocket.badge.domain.Seniority;
import fr.gravelinesgrimp.pocket.member.application.MyProfileView;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(
        name = "MyProfile",
        description = "Everything the member may see and edit about themselves.")
public record MyProfileResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) String lastName,
        String email,
        @Schema(requiredMode = RequiredMode.REQUIRED) Role role,
        String avatarUrl,
        LocalDate birthDate,
        @Schema(description = "Age today, when the birth date is known") Integer age,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean showAge,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "A parent or legal guardian agreed (declared under 15)")
                boolean parentalConsent,
        Integer clubArrivalYear,
        @Schema(description = "Years at the club (spec §9.4); absent without a year of arrival")
                Seniority seniority,
        Integer climbingSinceYear,
        String bio,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean appearsInRankings,
        @Schema(description = "My favourite route, possibly dismounted") UUID favoriteRouteId,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt) {

    public static MyProfileResponse from(MyProfileView view) {
        return new MyProfileResponse(
                view.id(),
                view.nickname(),
                view.firstName(),
                view.lastName(),
                view.email(),
                view.role(),
                MediaUrls.of(view.avatarId()),
                view.birthDate(),
                view.age(),
                view.showAge(),
                view.parentalConsent(),
                view.clubArrivalYear(),
                view.seniority(),
                view.climbingSinceYear(),
                view.bio(),
                view.appearsInRankings(),
                view.favoriteRouteId(),
                view.createdAt());
    }
}
