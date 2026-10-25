package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.YearMonth;

class PlanningMonthTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);

    @Test
    void thisMonthByDefault() {
        assertThat(PlanningMonth.parse(null, TODAY)).isEqualTo(YearMonth.of(2026, 10));
        assertThat(PlanningMonth.parse(" ", TODAY)).isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    void anyPastMonth() {
        assertThat(PlanningMonth.parse("2026-10", TODAY)).isEqualTo(YearMonth.of(2026, 10));
        assertThat(PlanningMonth.parse("2025-02", TODAY)).isEqualTo(YearMonth.of(2025, 2));
        assertThat(PlanningMonth.parse("2000-01", TODAY)).isEqualTo(YearMonth.of(2000, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11", "1999-12", "2026-13", "octobre", "2026-1", "2026-10-01"})
    void refusesFutureOrMalformedMonths(String value) {
        assertThatThrownBy(() -> PlanningMonth.parse(value, TODAY))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }
}
