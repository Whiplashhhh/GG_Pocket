package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.auth.application.Registration;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "RegisterRequest", description = "The one-page registration form (spec §6).")
public record RegisterRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 32)
                String code,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 50)
                String firstName,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(max = 50)
                String lastName,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Pattern(regexp = Nickname.PATTERN)
                String nickname,
        @Email @Size(max = 254) String email,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "8 characters minimum, not a common password")
                @NotNull
                @Size(max = 200)
                String password,
        @Past LocalDate birthDate,
        @Schema(description = "Required when the birth date says under 15") boolean parentalConsent,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "\"Tes données\" accepted")
                @AssertTrue
                boolean privacyAccepted,
        boolean rememberMe) {

    Registration toRegistration() {
        return new Registration(
                code,
                new MemberIdentity(nickname, firstName, lastName, email),
                password,
                birthDate,
                parentalConsent);
    }
}
