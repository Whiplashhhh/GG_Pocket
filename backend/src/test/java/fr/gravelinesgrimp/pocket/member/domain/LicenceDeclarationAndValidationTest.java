package fr.gravelinesgrimp.pocket.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDate;

class LicenceDeclarationAndValidationTest {

    private static final Instant SIGN_UP = Instant.parse("2026-10-31T18:00:00Z");
    private static final Instant DECISION = Instant.parse("2026-11-02T09:00:00Z");

    @Test
    void aLicensedPersonHasNothingToExplain() {
        assertThat(LicenceDeclaration.of(true, "  peu importe  "))
                .isEqualTo(new LicenceDeclaration(true, null));
    }

    @Test
    void aVisitorSaysWhoTheyAreAndTheNoteIsTrimmed() {
        assertThat(LicenceDeclaration.of(false, "  Copine de Lou, je viens l'été.  "))
                .isEqualTo(new LicenceDeclaration(false, "Copine de Lou, je viens l'été."));
    }

    @Test
    void theNoteLengthCountsCharactersNotBytes() {
        assertThat(LicenceDeclaration.of(false, "🧗".repeat(10)).joinNote()).hasSize(20);
        assertThat(LicenceDeclaration.of(false, "é".repeat(300)).joinNote()).hasSize(300);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "          ", "Un ami", "123456789"})
    void aVisitorWithoutAMeaningfulNoteIsRefused(String note) {
        assertThatThrownBy(() -> LicenceDeclaration.of(false, note))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.JOIN_NOTE_REQUIRED));
    }

    @Test
    void aNoteLongerThanThreeHundredCharactersIsRefused() {
        assertThatThrownBy(() -> LicenceDeclaration.of(false, "x".repeat(301)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void aNewAccountIsATouristAwaitingValidation() {
        var member = newcomer();

        assertThat(member.role()).isEqualTo(Role.TOURIST);
        assertThat(member.isValidated()).isFalse();
        assertThat(member.isVisibleInClub()).isFalse();
        assertThat(member.declaredLicensed()).isFalse();
        assertThat(member.joinNote()).isEqualTo("Collègue de Lou, le jeudi.");
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"TOURIST", "CLIMBER"})
    void theBoardMakesItATouristOrAClimberAndTheNoteIsErased(Role role) {
        var member = newcomer();

        member.validate(role, DECISION);

        assertThat(member.role()).isEqualTo(role);
        assertThat(member.isValidated()).isTrue();
        assertThat(member.isVisibleInClub()).isTrue();
        assertThat(member.joinNote()).isNull();
        assertThat(member.declaredLicensed()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"SETTER", "BOARD", "ADMIN"})
    void aNewAccountGetsNoHigherRoleAtOnce(Role role) {
        var member = newcomer();

        assertThatThrownBy(() -> member.validate(role, DECISION))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.VALIDATION_FAILED));
        assertThat(member.isValidated()).isFalse();
    }

    @Test
    void anAccountIsValidatedOnce() {
        var member = newcomer();
        member.validate(Role.CLIMBER, DECISION);

        assertThatThrownBy(() -> member.validate(Role.TOURIST, DECISION))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.MEMBER_ALREADY_VALIDATED));
        assertThat(member.role()).isEqualTo(Role.CLIMBER);
    }

    @Test
    void aDisabledMemberIsInvisibleEvenOnceValidated() {
        var member = newcomer();
        member.validate(Role.TOURIST, DECISION);
        member.disable();

        assertThat(member.isVisibleInClub()).isFalse();
    }

    private static Member newcomer() {
        return Member.awaitingValidation(
                new MemberIdentity("alix", "Alix", "Moreau", null),
                "hash",
                LocalDate.of(1995, 3, 2),
                false,
                LicenceDeclaration.of(false, "Collègue de Lou, le jeudi."),
                SIGN_UP);
    }
}
