package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.member.api.MediaUrls;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.social.application.MemberCardView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "MemberCard")
public record MemberCardResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) String lastName,
        String avatarUrl,
        @Schema(requiredMode = RequiredMode.REQUIRED) Role role,
        Integer clubArrivalYear) {

    static MemberCardResponse from(MemberCardView view) {
        return new MemberCardResponse(
                view.id(),
                view.nickname(),
                view.firstName(),
                view.lastName(),
                MediaUrls.of(view.avatarId()),
                view.role(),
                view.clubArrivalYear());
    }
}
