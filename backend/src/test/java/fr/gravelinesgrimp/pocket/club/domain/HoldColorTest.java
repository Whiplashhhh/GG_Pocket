package fr.gravelinesgrimp.pocket.club.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HoldColorTest {

    @Test
    void storesHexColoursInUpperCase() {
        var color = new HoldColor(" Fuchsia ", "#ff00aa", false, true, 15);
        assertThat(color.label()).isEqualTo("Fuchsia");
        assertThat(color.hex()).isEqualTo("#FF00AA");
    }

    @Test
    void refusesAnythingButRrGgBb() {
        assertThatThrownBy(() -> HoldColor.normalizeHex("F5C518"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HoldColor.normalizeHex("#F5C51"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
