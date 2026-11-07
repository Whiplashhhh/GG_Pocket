package fr.gravelinesgrimp.pocket.club.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class ClosedDayRangeTest {

    private static final LocalDate TODAY = LocalDate.parse("2026-11-07");

    @Test
    void aSingleDayWhenThereIsNoEnd() {
        var range = ClosedDayRange.of(LocalDate.parse("2026-12-25"), null, TODAY);

        assertThat(range.days()).containsExactly(LocalDate.parse("2026-12-25"));
        assertThat(range.describe()).isEqualTo("2026-12-25");
    }

    @Test
    void everyDayFromTheFirstToTheLastIncluded() {
        var range =
                ClosedDayRange.of(
                        LocalDate.parse("2026-12-30"), LocalDate.parse("2027-01-02"), TODAY);

        assertThat(range.days())
                .containsExactly(
                        LocalDate.parse("2026-12-30"),
                        LocalDate.parse("2026-12-31"),
                        LocalDate.parse("2027-01-01"),
                        LocalDate.parse("2027-01-02"));
        assertThat(range.describe()).isEqualTo("2026-12-30 / 2027-01-02");
    }

    @Test
    void aWholeYearAtMost() {
        var from = LocalDate.parse("2027-01-01");

        assertThat(ClosedDayRange.of(from, from.plusDays(365), TODAY).days()).hasSize(366);
        assertThatThrownBy(() -> ClosedDayRange.of(from, from.plusDays(366), TODAY))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refusesARangeBackwardsOrOutOfTheYearsTheAppKnows() {
        assertThatThrownBy(
                        () ->
                                ClosedDayRange.of(
                                        LocalDate.parse("2026-12-25"),
                                        LocalDate.parse("2026-12-24"),
                                        TODAY))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ClosedDayRange.of(LocalDate.parse("1999-12-31"), null, TODAY))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ClosedDayRange.of(LocalDate.parse("2029-01-01"), null, TODAY))
                .isInstanceOf(BusinessException.class);
    }
}
