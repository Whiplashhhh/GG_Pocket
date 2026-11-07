package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

class FreeSessionsTest {

    @Test
    void tuesdaysThursdaysAndSundays() {
        assertThat(FreeSessions.isFreeSessionDay(LocalDate.parse("2025-03-02"), Set.of())).isTrue();
        assertThat(FreeSessions.isFreeSessionDay(LocalDate.parse("2025-03-04"), Set.of())).isTrue();
        assertThat(FreeSessions.isFreeSessionDay(LocalDate.parse("2025-03-06"), Set.of())).isTrue();
        assertThat(FreeSessions.isFreeSessionDay(LocalDate.parse("2025-03-03"), Set.of()))
                .isFalse();
        assertThat(FreeSessions.isFreeSessionDay(LocalDate.parse("2025-03-08"), Set.of()))
                .isFalse();
    }

    @Test
    void notWhenTheGymIsClosed() {
        var christmas = LocalDate.parse("2025-12-25");

        assertThat(FreeSessions.isFreeSessionDay(christmas, Set.of())).isTrue();
        assertThat(FreeSessions.isFreeSessionDay(christmas, Set.of(christmas))).isFalse();
    }

    @Test
    void countsTheFreeSessionDaysOfAMonth() {
        var march = YearMonth.of(2025, 3);

        assertThat(FreeSessions.countIn(march, Set.of())).isEqualTo(13);
        assertThat(FreeSessions.countIn(march, Set.of(LocalDate.parse("2025-03-18"))))
                .isEqualTo(12);
        assertThat(FreeSessions.countIn(march, Set.of(LocalDate.parse("2025-03-17"))))
                .isEqualTo(13);
    }
}
