package fr.gravelinesgrimp.pocket.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode.State;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

class RegistrationCodeTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");

    @Test
    void generatedCodesUseTheReadableAlphabet() {
        var random = new Random(42);
        for (var i = 0; i < 200; i++) {
            var code = RegistrationCodeFormat.generate(random);
            assertThat(code).hasSize(8).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}");
        }
    }

    @Test
    void typingIsForgiving() {
        assertThat(RegistrationCodeFormat.normalize(" k7xm-p2qa ")).isEqualTo("K7XMP2QA");
        assertThat(RegistrationCodeFormat.normalize("K7XM P2QA")).isEqualTo("K7XMP2QA");
        assertThat(RegistrationCodeFormat.normalize(null)).isEmpty();
        assertThat(RegistrationCodeFormat.display("K7XMP2QA")).isEqualTo("K7XM-P2QA");
    }

    @Test
    void aFreshCodeWithoutLimitsIsUsable() {
        assertThat(code(null, null).state(NOW)).isEqualTo(State.USABLE);
    }

    @Test
    void expiresAtItsDate() {
        var code = code(NOW.plus(Duration.ofHours(1)), null);
        assertThat(code.isUsable(NOW)).isTrue();
        assertThat(code.state(NOW.plus(Duration.ofHours(1)))).isEqualTo(State.EXPIRED);
    }

    @Test
    void isExhaustedAfterItsLastUse() {
        var code = code(null, 2);
        code.recordUse();
        assertThat(code.isUsable(NOW)).isTrue();
        code.recordUse();
        assertThat(code.state(NOW)).isEqualTo(State.EXHAUSTED);
        assertThat(code.useCount()).isEqualTo(2);
    }

    @Test
    void anInactiveCodeIsRefusedWhateverItsLimits() {
        var code = code(null, null);
        code.deactivate();
        assertThat(code.state(NOW)).isEqualTo(State.INACTIVE);
        code.update("Affiche", null, null, true);
        assertThat(code.state(NOW)).isEqualTo(State.USABLE);
    }

    private static RegistrationCode code(Instant expiresAt, Integer maxUses) {
        return new RegistrationCode("K7XMP2QA", "Affiche", Role.CLIMBER, expiresAt, maxUses, null);
    }
}
