package fr.gravelinesgrimp.pocket.admin.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void shortensTextsToTheirColumn() {
        assertThat(AuditLog.shorten(null, 10)).isNull();
        assertThat(AuditLog.shorten("Grand toit", 10)).isEqualTo("Grand toit");
        assertThat(AuditLog.shorten("Grand dévers", 10)).isEqualTo("Grand dév…").hasSize(10);
    }

    @Test
    void neverCutsAnEmojiInHalf() {
        // "🧗" is two UTF-16 chars: cutting at 9 would split it.
        var shortened = AuditLog.shorten("Allez 12🧗 !!", 10);
        assertThat(shortened).isEqualTo("Allez 12…");
    }
}
