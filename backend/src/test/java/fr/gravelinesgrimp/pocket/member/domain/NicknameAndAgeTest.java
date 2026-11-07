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

    @Test
    void aMemberIsAMinorUntilTheEighteenthBirthday() {
        var birth = LocalDate.of(2008, 11, 7);
        assertThat(Age.isMinor(birth, LocalDate.of(2026, 11, 6))).isTrue();
        assertThat(Age.isMinor(birth, LocalDate.of(2026, 11, 7))).isFalse();
        assertThat(Age.isMinor(null, LocalDate.of(2026, 11, 7))).isFalse();
    }

    @Test
    void othersSeeTheAgeOfAnAdultWhoShowsItOnly() {
        var today = LocalDate.of(2026, 11, 7);
        assertThat(Age.shownToOthers(LocalDate.of(1990, 1, 1), true, today)).isEqualTo(36);
        assertThat(Age.shownToOthers(LocalDate.of(1990, 1, 1), false, today)).isNull();
        assertThat(Age.shownToOthers(LocalDate.of(2008, 11, 7), true, today)).isEqualTo(18);
        // A minor's age never shows, whatever was saved (D-119).
        assertThat(Age.shownToOthers(LocalDate.of(2008, 11, 8), true, today)).isNull();
        assertThat(Age.shownToOthers(null, true, today)).isNull();
    }
}
