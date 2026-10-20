package fr.gravelinesgrimp.pocket.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;

import java.util.Set;

class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy(Set.of("Azertyuiop", "motdepasse"));

    @Test
    void acceptsEightCharactersThatAreNotCommon() {
        assertThatCode(() -> policy.check("Dégaine7")).doesNotThrowAnyException();
    }

    @Test
    void refusesShortPasswords() {
        assertRefused("Sec!123", ErrorCode.PASSWORD_TOO_SHORT);
        assertRefused(null, ErrorCode.PASSWORD_TOO_SHORT);
    }

    @Test
    void countsCharactersNotBytes() {
        // 8 characters, but 4 of them take 2 bytes each in UTF-8.
        assertThatCode(() -> policy.check("éééébacs")).doesNotThrowAnyException();
    }

    @Test
    void refusesCommonPasswordsWhateverTheCase() {
        assertRefused("AZERTYUIOP", ErrorCode.PASSWORD_TOO_COMMON);
        assertRefused("MotDePasse", ErrorCode.PASSWORD_TOO_COMMON);
    }

    @Test
    void refusesWhatBcryptWouldTruncate() {
        assertThatCode(() -> policy.check("a".repeat(72))).doesNotThrowAnyException();
        assertRefused("a".repeat(73), ErrorCode.PASSWORD_TOO_LONG);
        assertRefused("é".repeat(37), ErrorCode.PASSWORD_TOO_LONG);
    }

    private void assertRefused(String password, ErrorCode code) {
        assertThatThrownBy(() -> policy.check(password))
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
