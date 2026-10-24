package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

class SessionCalendarTest {

    @Test
    void aMonthFreezesOnTheEighthOfTheNextMonthAtFourInGravelines() {
        var october = LocalDate.parse("2026-10-15");
        // 8 November 2026, 04:00 in Paris is 03:00 UTC (winter time).
        assertThat(SessionCalendar.isFrozen(october, Instant.parse("2026-11-08T02:59:59Z")))
                .isFalse();
        assertThat(SessionCalendar.isFrozen(october, Instant.parse("2026-11-08T03:00:00Z")))
                .isTrue();
        assertThat(SessionCalendar.isFrozen(october, Instant.parse("2026-10-31T23:00:00Z")))
                .isFalse();
    }

    @Test
    void theFreezeFollowsSummerTime() {
        // 8 April 2026, 04:00 in Paris is 02:00 UTC.
        var march = LocalDate.parse("2026-03-31");
        assertThat(SessionCalendar.isFrozen(march, Instant.parse("2026-04-08T01:59:00Z")))
                .isFalse();
        assertThat(SessionCalendar.isFrozen(march, Instant.parse("2026-04-08T02:00:00Z"))).isTrue();
    }

    @Test
    void decemberFreezesInJanuary() {
        var december = LocalDate.parse("2026-12-31");
        assertThat(SessionCalendar.isFrozen(december, Instant.parse("2027-01-08T02:59:00Z")))
                .isFalse();
        assertThat(SessionCalendar.isFrozen(december, Instant.parse("2027-01-08T03:00:00Z")))
                .isTrue();
    }

    @Test
    void aNewSessionIsDatedTodayOrUpToSevenDaysBack() {
        // 23 October 2026, 10:00 in Paris.
        var now = Instant.parse("2026-10-23T08:00:00Z");
        assertThatCode(() -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-23"), now))
                .doesNotThrowAnyException();
        assertThatCode(() -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-16"), now))
                .doesNotThrowAnyException();
        assertCode(
                () -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-15"), now),
                ErrorCode.SESSION_TOO_OLD);
        assertCode(
                () -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-24"), now),
                ErrorCode.DATE_IN_THE_FUTURE);
    }

    @Test
    void todayIsTheDayInGravelines() {
        // 23 October 2026, 23:30 UTC is already the 24th in Paris.
        var now = Instant.parse("2026-10-23T22:30:00Z");
        assertThatCode(() -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-24"), now))
                .doesNotThrowAnyException();
    }

    @Test
    void theLastDaysOfTheMonthStayOpenUntilTheEighth() {
        // 7 November 2026: 31 October is 7 days back and October is not frozen yet.
        var now = Instant.parse("2026-11-07T12:00:00Z");
        assertThatCode(() -> SessionCalendar.checkNewDate(LocalDate.parse("2026-10-31"), now))
                .doesNotThrowAnyException();
        assertThatCode(() -> SessionCalendar.checkNotFrozen(LocalDate.parse("2026-10-01"), now))
                .doesNotThrowAnyException();
        assertCode(
                () ->
                        SessionCalendar.checkNotFrozen(
                                LocalDate.parse("2026-10-01"),
                                Instant.parse("2026-11-08T03:00:00Z")),
                ErrorCode.MONTH_FROZEN);
    }

    private static void assertCode(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
