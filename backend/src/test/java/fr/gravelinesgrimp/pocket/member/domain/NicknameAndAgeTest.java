package fr.gravelinesgrimp.pocket.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

class NicknameAndAgeTest {

    @ParameterizedTest
    @ValueSource(
            strings = {"Marie", "jo_", "tonton.talon", "Bye-Bye-Morice", "a1234567890123456789"})
    void acceptsSimpleNicknames(String nickname) {
        assertThat(Nickname.isValid(nickname)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ab", "Élodie", "avec espace", "a12345678901234567890", "o'neil"})
    void refusesOtherNicknames(String nickname) {
        assertThat(Nickname.isValid(nickname)).isFalse();
    }

    @Test
    void nicknamesCompareWhateverTheCase() {
        assertThat(Nickname.key(" Marie ")).isEqualTo(Nickname.key("mARIE"));
    }

    @Test
    void parentalConsentIsNeededUntilTheFifteenthBirthday() {
        var birth = LocalDate.of(2011, 10, 21);
        assertThat(Age.needsParentalConsent(birth, LocalDate.of(2026, 10, 20))).isTrue();
        assertThat(Age.needsParentalConsent(birth, LocalDate.of(2026, 10, 21))).isFalse();
        assertThat(Age.needsParentalConsent(null, LocalDate.of(2026, 10, 21))).isFalse();
    }
}
