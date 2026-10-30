package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class SeniorityTest {

    @ParameterizedTest
    @CsvSource({
        "0, FRESH",
        "1, BABY_CARABINER",
        "2, PRO_BEGINNER",
        "4, PRO_BEGINNER",
        "5, FORCER",
        "9, FORCER",
        "10, RESIDENT",
        "14, RESIDENT",
        "15, FOSSIL",
        "40, FOSSIL"
    })
    void followsTheYearsAtTheClub(int years, Seniority expected) {
        assertThat(Seniority.of(2026 - years, 2026)).isEqualTo(expected);
    }

    @Test
    void unknownWithoutAYearOfArrival() {
        assertThat(Seniority.of(null, 2026)).isNull();
    }

    @Test
    void aYearAheadIsStillFresh() {
        assertThat(Seniority.of(2027, 2026)).isEqualTo(Seniority.FRESH);
    }

    @Test
    void theNewYearComesAtMidnightInGravelines() {
        // 23:30 UTC on 31 December is already 00:30 on 1 January in Gravelines.
        var clock = Clock.fixed(Instant.parse("2025-12-31T23:30:00Z"), ZoneOffset.UTC);

        assertThat(Seniority.today(2025, clock)).isEqualTo(Seniority.BABY_CARABINER);
    }
}
