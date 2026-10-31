package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.MemberAdminService.MemberDetailView;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(name = "MemberDetail", description = "The member sheet, with what the caller may do.")
public record MemberDetailResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) MemberSummaryResponse member,
        String email,
        @Schema(
                        description =
                                "Who a person not licensed is to the club, until the board decides"
                                        + " on the account")
                String joinNote,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Under 15, with a parental consent declared (no birth date)")
                boolean underFifteenWithParentalConsent,
        Integer clubArrivalYear,
        Integer climbingSinceYear,
        @Schema(requiredMode = RequiredMode.REQUIRED) PermissionsResponse permissions) {

    @Schema(name = "MemberPermissions")
    public record PermissionsResponse(
            @Schema(requiredMode = RequiredMode.REQUIRED) List<Role> assignableRoles,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean canDisable,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean canEnable,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean canCreateResetLink,
            @Schema(
                            requiredMode = RequiredMode.REQUIRED,
                            description =
                                    "Delete the account for good (admin, not oneself), or refuse"
                                            + " an account never validated (board)")
                    boolean canDelete,
            @Schema(
                            requiredMode = RequiredMode.REQUIRED,
                            description = "Validate this new account as a tourist or a climber")
                    boolean canValidate) {}

    static MemberDetailResponse from(MemberDetailView view) {
        var permissions = view.permissions();
        return new MemberDetailResponse(
                MemberSummaryResponse.from(view.summary()),
                view.email(),
                view.joinNote(),
                view.underFifteenWithParentalConsent(),
                view.clubArrivalYear(),
                view.climbingSinceYear(),
                new PermissionsResponse(
                        permissions.assignableRoles().stream().sorted().toList(),
                        permissions.canDisable(),
                        permissions.canEnable(),
                        permissions.canCreateResetLink(),
                        permissions.canDelete(),
                        permissions.canValidate()));
    }
}
