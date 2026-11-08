package fr.gravelinesgrimp.pocket.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.member.domain.Inactivity.Verdict;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

/** D-119: a year without a visit puts an account to sleep, two years delete it. */
class InactivityTest {

    /** 2026-11-07 03:15 in Gravelines (winter time). */
    private static final Instant NOW = Instant.parse("2026-11-07T02:15:00Z");

    private static final Instant ONE_YEAR_BACK = Instant.parse("2025-11-07T02:15:00Z");
    private static final Instant TWO_YEARS_BACK = Instant.parse("2024-11-07T02:15:00Z");
    private static final Duration A_SECOND = Duration.ofSeconds(1);

    @Test
    void aYearIsTheSameDayAndTimeAYearBackInGravelines() {
        assertThat(Inactivity.yearsBack(NOW, 1)).isEqualTo(ONE_YEAR_BACK);
        // Summer time on one side, winter time on the other: still the same wall-clock time.
        assertThat(Inactivity.yearsBack(Instant.parse("2026-07-01T10:00:00Z"), 1))
                .isEqualTo(Instant.parse("2025-07-01T10:00:00Z"));
        // A 29 February counts back to the 28th.
        assertThat(Inactivity.yearsBack(Instant.parse("2028-02-29T11:00:00Z"), 1))
                .isEqualTo(Instant.parse("2027-02-28T11:00:00Z"));
    }

    @Test
    void exactlyOneYearAfterTheLastVisitTheAccountIsKept() {
        assertThat(Inactivity.verdict(Role.CLIMBER, true, ONE_YEAR_BACK, NOW))
                .isEqualTo(Verdict.KEEP);
        assertThat(Inactivity.verdict(Role.CLIMBER, true, ONE_YEAR_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.PUT_TO_SLEEP);
        // 365 days back is exactly a year here (no 29 February in between): still kept.
        assertThat(Inactivity.verdict(Role.CLIMBER, true, NOW.minus(Duration.ofDays(365)), NOW))
                .isEqualTo(Verdict.KEEP);
    }

    @Test
    void exactlyTwoYearsAfterTheLastVisitTheAccountIsNotYetDeleted() {
        assertThat(Inactivity.verdict(Role.CLIMBER, false, TWO_YEARS_BACK, NOW))
                .isEqualTo(Verdict.KEEP);
        assertThat(Inactivity.verdict(Role.CLIMBER, false, TWO_YEARS_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.DELETE);
        // An account still active two years on (the job missed a day) is deleted all the same.
        assertThat(Inactivity.verdict(Role.SETTER, true, TWO_YEARS_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.DELETE);
    }

    @Test
    void aDisabledAccountIsNeverPutToSleepAgain() {
        assertThat(Inactivity.verdict(Role.CLIMBER, false, ONE_YEAR_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.KEEP);
    }

    @Test
    void adminsAreNeverTouched() {
        assertThat(Inactivity.verdict(Role.ADMIN, true, TWO_YEARS_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.KEEP);
        assertThat(Inactivity.verdict(Role.ADMIN, false, TWO_YEARS_BACK.minus(A_SECOND), NOW))
                .isEqualTo(Verdict.KEEP);
    }

    @Test
    void theLastVisitIsTheLatestSignOfLife() {
        var registered = Instant.parse("2023-01-01T10:00:00Z");
        var seen = Instant.parse("2024-06-01T10:00:00Z");
        var reactivated = Instant.parse("2026-03-01T10:00:00Z");
        assertThat(Inactivity.lastVisit(registered, null, null)).isEqualTo(registered);
        assertThat(Inactivity.lastVisit(registered, seen, null)).isEqualTo(seen);
        assertThat(Inactivity.lastVisit(registered, seen, reactivated)).isEqualTo(reactivated);
        assertThat(Inactivity.lastVisit(registered, reactivated, seen)).isEqualTo(reactivated);
    }
}
