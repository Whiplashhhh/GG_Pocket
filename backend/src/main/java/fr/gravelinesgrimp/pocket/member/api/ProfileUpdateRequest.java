package fr.gravelinesgrimp.pocket.member.api;

import fr.gravelinesgrimp.pocket.member.application.ProfileChange;
import fr.gravelinesgrimp.pocket.member.domain.MemberDetails;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "ProfileUpdate", description = "The whole editable profile, as the member wants it.")
public record ProfileUpdateRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Pattern(regexp = Nickname.PATTERN)
                String nickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 50)
                String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 50)
                String lastName,
        @Email @Size(max = 254) String email,
        @Past LocalDate birthDate,
        @Schema(description = "Declares that a parent agreed; required once under 15")
                boolean parentalConsent,
        boolean showAge,
        @Min(1950) @Max(2100) Integer clubArrivalYear,
        @Min(1950) @Max(2100) Integer climbingSinceYear,
        @PlainText @Size(max = 160) String bio,
        boolean appearsInRankings) {

    ProfileChange toChange() {
        return new ProfileChange(
                new MemberIdentity(nickname, firstName, lastName, email),
                new MemberDetails(
                        clubArrivalYear, climbingSinceYear, bio, showAge, appearsInRankings),
                birthDate,
                parentalConsent);
    }
}
