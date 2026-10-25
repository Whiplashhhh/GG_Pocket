package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class StreakTest {

    /** A Wednesday: this week runs from Monday the 19th to Sunday the 25th. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);

    @Test
    void noClimbingNoStreak() {
        assertThat(Streak.of(List.of(), TODAY)).isEqualTo(new Streak(0, 2, 0));
    }

    @Test
    void weeksWithTwoDaysInARowMakeTheStreak() {
        var days =
                List.of(
                        day(13),
                        day(15), // last week
                        day(6),
                        day(7),
                        day(11), // the week before
                        LocalDate.of(2026, 9, 29),
                        LocalDate.of(2026, 10, 1)); // and before
        assertThat(Streak.of(days, TODAY).weeks()).isEqualTo(3);
    }

    @Test
    void thisWeekCountsOnceItHasItsTwoDays() {
        var days = List.of(day(19), day(20), day(13), day(14));
        assertThat(Streak.of(days, TODAY)).isEqualTo(new Streak(2, 2, 2));
    }

    @Test
    void anUnfinishedWeekDoesNotBreakTheStreak() {
        var days = List.of(day(19), day(13), day(14));
        assertThat(Streak.of(days, TODAY)).isEqualTo(new Streak(1, 2, 1));
    }

    @Test
    void aWeekWithOneDayBreaksIt() {
        var days = List.of(day(13), day(14), day(8), day(1), day(2));
        assertThat(Streak.of(days, TODAY).weeks()).isEqualTo(1);
    }

    @Test
    void twoSessionsOnTheSameDayAreOneClimbingDay() {
        var days = List.of(day(13), day(13), day(14), day(6), day(6));
        assertThat(Streak.of(days, TODAY).weeks()).isEqualTo(1);
    }

    @Test
    void weeksRunFromMondayToSunday() {
        // Sunday the 18th and Monday the 12th are in the same ISO week.
        var days = List.of(day(18), day(12));
        assertThat(Streak.of(days, TODAY).weeks()).isEqualTo(1);
        assertThat(Streak.of(List.of(day(18), day(19)), TODAY)).isEqualTo(new Streak(0, 2, 1));
    }

    private static LocalDate day(int dayOfOctober) {
        return LocalDate.of(2026, 10, dayOfOctober);
    }
}
