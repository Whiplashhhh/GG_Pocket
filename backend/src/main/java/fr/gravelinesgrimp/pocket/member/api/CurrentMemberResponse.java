package fr.gravelinesgrimp.pocket.member.api;

import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "CurrentMember", description = "The signed-in member, as shown in the app shell.")
public record CurrentMemberResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) String lastName,
        @Schema(requiredMode = RequiredMode.REQUIRED) Role role,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "False while the board has not validated the account: the member"
                                        + " only has their own logbook until then")
                boolean validated,
        @Schema(description = "Profile photo, absent when the member has none") String avatarUrl) {

    public static CurrentMemberResponse from(CurrentMemberView view) {
        return new CurrentMemberResponse(
                view.id(),
                view.nickname(),
                view.firstName(),
                view.lastName(),
                view.role(),
                view.validated(),
                MediaUrls.of(view.avatarId()));
    }
}
