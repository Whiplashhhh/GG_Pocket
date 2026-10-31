package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** One nested class per badge of the catalogue (spec §9.5), with its limits. */
class BadgeEngineTest {

    private final List<ClimbRecord> history = new ArrayList<>();
    private Instant clock = Instant.parse("2020-01-01T00:00:00Z");

    @Nested
    class ToppedTiers {

        @ParameterizedTest
        @MethodSource("fr.gravelinesgrimp.pocket.badge.domain.BadgeEngineTest#toppedTiers")
        void comeAtTheExactCount(BadgeCode tier) {
            session("2025-03-03", tops(tier.threshold() - 1));
            assertThat(count(tier)).isZero();

            var last = session("2025-03-04", top("4a"));

            assertThat(only(tier).sessionId()).isEqualTo(last.sessionId());
            assertThat(only(tier).earnedOn()).isEqualTo(LocalDate.parse("2025-03-04"));
        }

        @Test
        void countAscentsNotRoutes() {
            var route = UUID.randomUUID();
            session(
                    "2025-03-03",
                    IntStream.range(0, 5)
                            .mapToObj(i -> line(route, ClimbMode.TOP_ROPE, true))
                            .toList());

            assertThat(count(BadgeCode.TOPPED_5)).isOne();
        }

        @Test
        void leaveOutTheAscentsNotTopped() {
            session("2025-03-03", line(UUID.randomUUID(), ClimbMode.LEAD, false));

            assertThat(count(BadgeCode.TOPPED_1)).isZero();
            assertThat(count(BadgeCode.LEAD_1)).isZero();
        }

        @Test
        void areEarnedOnceEach() {
            session("2025-03-03", tops(25));

            assertThat(codes())
                    .containsOnlyOnce(
                            BadgeCode.TOPPED_1,
                            BadgeCode.TOPPED_5,
                            BadgeCode.TOPPED_10,
                            BadgeCode.TOPPED_20)
                    .doesNotContain(BadgeCode.TOPPED_50);
        }
    }

    @Nested
    class LeadTiers {

        @ParameterizedTest
        @MethodSource("fr.gravelinesgrimp.pocket.badge.domain.BadgeEngineTest#leadTiers")
        void comeAtTheExactCount(BadgeCode tier) {
            session("2025-03-03", leads(tier.threshold() - 1));
            assertThat(count(tier)).isZero();

            var last = session("2025-03-04", leads(1));

            assertThat(only(tier).sessionId()).isEqualTo(last.sessionId());
        }

        @Test
        void leaveOutTopRope() {
            session("2025-03-03", tops(3));

            assertThat(count(BadgeCode.LEAD_1)).isZero();
            assertThat(count(BadgeCode.TOPPED_1)).isOne();
        }
    }

    @Nested
    class FirstGrade {

        @Test
        void aMinusIsBelowItsGrade() {
            session("2025-03-03", top("6a-"));

            assertThat(codes()).contains(BadgeCode.GRADE_5C).doesNotContain(BadgeCode.GRADE_6A);
        }

        @Test
        void theGradeOrAPlusEarnsIt() {
            session("2025-03-03", top("6a"));
            session("2025-03-04", top("6b+"));

            assertThat(only(BadgeCode.GRADE_6A).earnedOn())
                    .isEqualTo(LocalDate.parse("2025-03-03"));
            assertThat(only(BadgeCode.GRADE_6B).earnedOn())
                    .isEqualTo(LocalDate.parse("2025-03-04"));
        }

        @Test
        void aHardRouteStraightAwayEarnsEveryLowerTier() {
            session("2025-03-03", top("7a"));

            assertThat(codes())
                    .contains(
                            BadgeCode.GRADE_5B,
                            BadgeCode.GRADE_5C,
                            BadgeCode.GRADE_6A,
                            BadgeCode.GRADE_6B,
                            BadgeCode.GRADE_6C,
                            BadgeCode.GRADE_7A)
                    .doesNotContain(BadgeCode.GRADE_7B, BadgeCode.GRADE_7C);
        }

        @Test
        void the7cPlusEarnsTheLastTier() {
            session("2025-03-03", top("7c+"));

            assertThat(count(BadgeCode.GRADE_7C)).isOne();
        }

        @Test
        void needsTheTop() {
            session(
                    "2025-03-03",
                    new ClimbRecord.Line(
                            UUID.randomUUID(), ClimbMode.LEAD, false, false, false, "7a", null));

            assertThat(codes()).doesNotContain(BadgeCode.GRADE_5B);
        }

        @Test
        void comesOnceWhateverTheMode() {
            session("2025-03-03", top("5b"), leadOn("5b"));

            assertThat(count(BadgeCode.GRADE_5B)).isOne();
        }
    }

    @Nested
    class ClimbingDayTiers {

        @ParameterizedTest
        @MethodSource("fr.gravelinesgrimp.pocket.badge.domain.BadgeEngineTest#dayTiers")
        void comeAtTheExactNumberOfDays(BadgeCode tier) {
            var day = LocalDate.parse("2020-01-01");
            for (int i = 1; i < tier.threshold(); i++) {
                session(day.toString());
                day = day.plusDays(1);
            }
            assertThat(count(tier)).isZero();

            session(day.toString());

            assertThat(count(tier)).isOne();
        }

        @Test
        void twoSessionsOnTheSameDayCountOnce() {
            for (int i = 1; i <= 9; i++) {
                session("2025-01-%02d".formatted(i));
            }
            session("2025-01-09");
            assertThat(count(BadgeCode.DAYS_10)).isZero();

            var tenth = session("2025-01-10");
            session("2025-01-10");

            assertThat(only(BadgeCode.DAYS_10).sessionId()).isEqualTo(tenth.sessionId());
        }
    }

    @Nested
    class Weeks {

        @Test
        void twoDaysInAWeekEarnWeek2Only() {
            session("2025-03-03");
            var second = session("2025-03-05");

            assertThat(only(BadgeCode.WEEK_2).sessionId()).isEqualTo(second.sessionId());
            assertThat(only(BadgeCode.WEEK_2).key()).isEqualTo("2025-03-03");
            assertThat(codes()).doesNotContain(BadgeCode.WEEK_3, BadgeCode.WEEK_4);
        }

        @Test
        void fourDaysEarnWeek2And3And4() {
            session("2025-03-03");
            session("2025-03-04");
            session("2025-03-06");
            session("2025-03-09");

            assertThat(codes())
                    .containsOnlyOnce(BadgeCode.WEEK_2, BadgeCode.WEEK_3, BadgeCode.WEEK_4);
        }

        @Test
        void threeDaysStopAtWeek3() {
            session("2025-03-03");
            session("2025-03-04");
            session("2025-03-09");

            assertThat(codes()).contains(BadgeCode.WEEK_3).doesNotContain(BadgeCode.WEEK_4);
        }

        @Test
        void twoSessionsTheSameDayAreOneDay() {
            session("2025-03-03");
            session("2025-03-03");

            assertThat(count(BadgeCode.WEEK_2)).isZero();
        }

        @Test
        void aWeekOverTwoMonthsIsOneWeek() {
            session("2025-09-30");
            session("2025-10-02");

            assertThat(only(BadgeCode.WEEK_2).key()).isEqualTo("2025-09-29");
        }

        @Test
        void sundayAndMondayAreTwoWeeks() {
            session("2025-03-09");
            session("2025-03-10");

            assertThat(count(BadgeCode.WEEK_2)).isZero();
        }

        @Test
        void comeOncePerWeek() {
            session("2025-03-03");
            session("2025-03-04");
            session("2025-03-17");
            session("2025-03-18");

            assertThat(count(BadgeCode.WEEK_2)).isEqualTo(2);
        }
    }

    @Nested
    class Months {

        @Test
        void tenDaysCompleteTheMonth() {
            daysOfMarch(9);
            assertThat(count(BadgeCode.MONTH_COMPLETE)).isZero();

            session("2025-03-31");

            assertThat(only(BadgeCode.MONTH_COMPLETE).key()).isEqualTo("2025-03");
            assertThat(only(BadgeCode.MONTH_COMPLETE).earnedOn())
                    .isEqualTo(LocalDate.parse("2025-03-31"));
        }

        @Test
        void theFirstOfTheNextMonthDoesNotCount() {
            daysOfMarch(9);
            session("2025-04-01");

            assertThat(count(BadgeCode.MONTH_COMPLETE)).isZero();
        }

        @Test
        void twelveDaysEarnBoth() {
            daysOfMarch(11);
            assertThat(count(BadgeCode.MONTH_12)).isZero();

            session("2025-03-31");

            assertThat(codes()).containsOnlyOnce(BadgeCode.MONTH_COMPLETE, BadgeCode.MONTH_12);
        }

        @Test
        void comeOncePerMonth() {
            daysOfMarch(15);
            for (int day = 1; day <= 10; day++) {
                session("2025-04-%02d".formatted(day));
            }

            assertThat(count(BadgeCode.MONTH_COMPLETE)).isEqualTo(2);
            assertThat(count(BadgeCode.MONTH_12)).isOne();
        }

        private void daysOfMarch(int days) {
            for (int day = 1; day <= days; day++) {
                session("2025-03-%02d".formatted(day));
            }
        }
    }

    @Nested
    class ThreeWeekStreak {

        @Test
        void threeWeeksInARowEarnIt() {
            var last = twoDaysInWeeks("2025-03-03", 3);

            assertThat(only(BadgeCode.STREAK_3W).key()).isEqualTo("2025-03-03");
            assertThat(only(BadgeCode.STREAK_3W).sessionId()).isEqualTo(last.sessionId());
        }

        @Test
        void twoWeeksAreNotEnough() {
            twoDaysInWeeks("2025-03-03", 2);

            assertThat(count(BadgeCode.STREAK_3W)).isZero();
        }

        @Test
        void blocksDoNotOverlap() {
            twoDaysInWeeks("2025-03-03", 5);
            assertThat(count(BadgeCode.STREAK_3W)).isOne();

            twoDaysInWeeks("2025-04-07", 1);

            assertThat(awards())
                    .filteredOn(award -> award.code() == BadgeCode.STREAK_3W)
                    .extracting(Award::key)
                    .containsExactly("2025-03-03", "2025-03-24");
        }

        @Test
        void aWeekWithOneDayBreaksIt() {
            twoDaysInWeeks("2025-03-03", 2);
            session("2025-03-17");
            twoDaysInWeeks("2025-03-24", 2);

            assertThat(count(BadgeCode.STREAK_3W)).isZero();
        }

        @Test
        void runsAcrossMonths() {
            twoDaysInWeeks("2025-03-17", 3);

            assertThat(count(BadgeCode.STREAK_3W)).isOne();
        }

        private ClimbRecord twoDaysInWeeks(String firstMonday, int weeks) {
            var monday = LocalDate.parse(firstMonday);
            ClimbRecord last = null;
            for (int week = 0; week < weeks; week++) {
                session(monday.toString());
                last = session(monday.plusDays(2).toString());
                monday = monday.plusWeeks(1);
            }
            return last;
        }
    }

    @Nested
    class MonthWithoutMissingAWeek {

        // September 2025: its weeks start on the 1st, 8th, 15th, 22nd and 29th.

        @Test
        void everyWeekStartingInTheMonthEarnsIt() {
            session("2025-09-01");
            session("2025-09-12");
            session("2025-09-15");
            session("2025-09-28");
            var last = session("2025-10-02");

            assertThat(only(BadgeCode.MONTH_NO_MISS).key()).isEqualTo("2025-09");
            assertThat(only(BadgeCode.MONTH_NO_MISS).sessionId()).isEqualTo(last.sessionId());
        }

        @Test
        void oneMissingWeekLosesIt() {
            session("2025-09-01");
            session("2025-09-12");
            session("2025-09-28");
            session("2025-10-02");

            assertThat(count(BadgeCode.MONTH_NO_MISS)).isZero();
        }

        @Test
        void theLastWeekIsNeeded() {
            session("2025-09-01");
            session("2025-09-08");
            session("2025-09-15");
            session("2025-09-22");

            assertThat(count(BadgeCode.MONTH_NO_MISS)).isZero();
        }

        @Test
        void aWeekBelongsToTheMonthOfItsMonday() {
            // October 2025 starts on a Wednesday: the 1st belongs to September's last week.
            session("2025-10-01");
            session("2025-10-06");
            session("2025-10-13");
            session("2025-10-20");
            session("2025-10-27");

            assertThat(awards())
                    .filteredOn(award -> award.code() == BadgeCode.MONTH_NO_MISS)
                    .extracting(Award::key)
                    .containsExactly("2025-10");
        }
    }

    @Nested
    class FullLead {

        @Test
        void threeLeadsEarnIt() {
            var full = session("2025-03-03", leads(3));

            assertThat(only(BadgeCode.FULL_LEAD).sessionId()).isEqualTo(full.sessionId());
        }

        @Test
        void twoAreNotEnough() {
            session("2025-03-03", leads(2));

            assertThat(count(BadgeCode.FULL_LEAD)).isZero();
        }

        @Test
        void oneTopRopeSpoilsIt() {
            var lines = new ArrayList<>(leads(3));
            lines.add(top("4a"));
            session("2025-03-03", lines);

            assertThat(count(BadgeCode.FULL_LEAD)).isZero();
        }

        @Test
        void fallsInLeadStillCount() {
            session(
                    "2025-03-03",
                    Collections.nCopies(3, line(UUID.randomUUID(), ClimbMode.LEAD, false)));

            assertThat(count(BadgeCode.FULL_LEAD)).isOne();
        }

        @Test
        void comesOncePerSession() {
            session("2025-03-03", leads(3));
            session("2025-03-03", leads(4));

            assertThat(count(BadgeCode.FULL_LEAD)).isEqualTo(2);
        }
    }

    @Nested
    class Perfect {

        @Test
        void threeCleanAscentsEarnIt() {
            session("2025-03-03", cleans(3));

            assertThat(count(BadgeCode.PERFECT)).isOne();
        }

        @Test
        void twoAreNotEnough() {
            session("2025-03-03", cleans(2));

            assertThat(count(BadgeCode.PERFECT)).isZero();
        }

        @Test
        void oneFallSpoilsIt() {
            var lines = new ArrayList<>(cleans(3));
            lines.add(top("4a"));
            session("2025-03-03", lines);

            assertThat(count(BadgeCode.PERFECT)).isZero();
        }
    }

    @Nested
    class ZeroRest {

        @Test
        void tenToppedAscentsEarnIt() {
            session("2025-03-03", tops(10));

            assertThat(count(BadgeCode.ZERO_REST)).isOne();
        }

        @Test
        void nineAreNotEnough() {
            var lines = new ArrayList<>(tops(9));
            lines.add(line(UUID.randomUUID(), ClimbMode.TOP_ROPE, false));
            session("2025-03-03", lines);

            assertThat(count(BadgeCode.ZERO_REST)).isZero();
        }

        @Test
        void twoSessionsOfTheSameDayDoNotAddUp() {
            session("2025-03-03", tops(5));
            session("2025-03-03", tops(5));

            assertThat(count(BadgeCode.ZERO_REST)).isZero();
        }
    }

    @Nested
    class Comeback {

        @Test
        void thirtyDaysAfterTheLastDayEarnIt() {
            session("2025-03-01");
            var back = session("2025-03-31");

            assertThat(only(BadgeCode.COMEBACK).sessionId()).isEqualTo(back.sessionId());
            assertThat(only(BadgeCode.COMEBACK).key()).isEqualTo("2025-03-31");
        }

        @Test
        void twentyNineDaysAreNotEnough() {
            session("2025-03-01");
            session("2025-03-30");

            assertThat(count(BadgeCode.COMEBACK)).isZero();
        }

        @Test
        void theVeryFirstSessionIsNoComeback() {
            session("2025-03-01");

            assertThat(count(BadgeCode.COMEBACK)).isZero();
        }

        @Test
        void comesOncePerReturnDay() {
            session("2025-01-01");
            session("2025-03-01");
            session("2025-03-01");
            session("2025-03-02");
            session("2025-05-01");

            assertThat(count(BadgeCode.COMEBACK)).isEqualTo(2);
        }
    }

    @Nested
    class Flash {

        private final LocalDate climbed = LocalDate.parse("2025-03-20");

        @Test
        void aCleanFirstTryOnAFreshRouteEarnsIt() {
            var route = UUID.randomUUID();
            var flash = session(climbed.toString(), flash(route, climbed.minusDays(14)));

            assertThat(only(BadgeCode.FLASH).sessionId()).isEqualTo(flash.sessionId());
            assertThat(only(BadgeCode.FLASH).key()).isEqualTo(route.toString());
        }

        @Test
        void fifteenDaysIsTooOld() {
            session(climbed.toString(), flash(UUID.randomUUID(), climbed.minusDays(15)));

            assertThat(count(BadgeCode.FLASH)).isZero();
        }

        @Test
        void theOpeningDayCounts() {
            session(climbed.toString(), flash(UUID.randomUUID(), climbed));

            assertThat(count(BadgeCode.FLASH)).isOne();
        }

        @Test
        void needsBothCleanAndFirstTry() {
            session(
                    climbed.toString(),
                    new ClimbRecord.Line(
                            UUID.randomUUID(), ClimbMode.LEAD, true, true, false, "6a", climbed),
                    new ClimbRecord.Line(
                            UUID.randomUUID(), ClimbMode.LEAD, true, false, true, "6a", climbed));

            assertThat(count(BadgeCode.FLASH)).isZero();
        }

        @Test
        void needsAnOpeningDate() {
            session(climbed.toString(), flash(UUID.randomUUID(), null));

            assertThat(count(BadgeCode.FLASH)).isZero();
        }

        @Test
        void aRouteIsFlashedOnce() {
            var route = UUID.randomUUID();
            session(climbed.toString(), flash(route, climbed));
            session(climbed.plusDays(2).toString(), flash(route, climbed));

            assertThat(count(BadgeCode.FLASH)).isOne();
        }

        @Test
        void comesOncePerRoute() {
            session(
                    climbed.toString(),
                    flash(UUID.randomUUID(), climbed),
                    flash(UUID.randomUUID(), climbed));

            assertThat(count(BadgeCode.FLASH)).isEqualTo(2);
        }
    }

    @Nested
    class Record {

        @Test
        void theFourthSessionCanSetIt() {
            session("2025-03-01", tops(2));
            session("2025-03-02", tops(3));
            session("2025-03-03", tops(1));
            var record = session("2025-03-04", tops(4));

            assertThat(only(BadgeCode.RECORD).sessionId()).isEqualTo(record.sessionId());
        }

        @Test
        void theThirdCannot() {
            session("2025-03-01", tops(1));
            session("2025-03-02", tops(2));
            session("2025-03-03", tops(3));

            assertThat(count(BadgeCode.RECORD)).isZero();
        }

        @Test
        void equallingIsNotBeating() {
            session("2025-03-01", tops(5));
            session("2025-03-02", tops(1));
            session("2025-03-03", tops(1));
            session("2025-03-04", tops(5));

            assertThat(count(BadgeCode.RECORD)).isZero();
        }

        @Test
        void eachNewRecordCounts() {
            session("2025-03-01", tops(1));
            session("2025-03-02", tops(1));
            session("2025-03-03", tops(1));
            session("2025-03-04", tops(2));
            session("2025-03-05", tops(2));
            session("2025-03-06", tops(3));

            assertThat(count(BadgeCode.RECORD)).isEqualTo(2);
        }
    }

    @Nested
    class Replay {

        @Test
        void theOrderOfTheHistoryDoesNotMatter() {
            session("2025-03-03", tops(3));
            session("2025-03-05", leads(4));
            session("2025-03-04", cleans(3));
            var inOrder = awards();

            Collections.reverse(history);

            assertThat(awards()).containsExactlyInAnyOrderElementsOf(inOrder);
        }

        @Test
        void sessionsOfADayFollowTheirSaveTime() {
            var first = session("2025-03-03", tops(1));
            session("2025-03-03", tops(1));
            Collections.reverse(history);

            assertThat(only(BadgeCode.TOPPED_1).sessionId()).isEqualTo(first.sessionId());
        }

        @Test
        void aLaterSessionNeverMovesEarlierAwards() {
            session("2025-03-03", tops(6));
            session("2025-03-04", leads(3));
            var before = awards();

            session("2025-03-10", tops(30));

            assertThat(awards()).containsAll(before);
        }

        @Test
        void noHistoryNoBadge() {
            assertThat(awards()).isEmpty();
        }
    }

    // --- history ---

    private ClimbRecord session(String day, ClimbRecord.Line... lines) {
        return session(day, List.of(lines));
    }

    private ClimbRecord session(String day, List<ClimbRecord.Line> lines) {
        clock = clock.plusSeconds(60);
        var session = new ClimbRecord(UUID.randomUUID(), LocalDate.parse(day), clock, lines);
        history.add(session);
        return session;
    }

    private static ClimbRecord.Line line(UUID route, ClimbMode mode, boolean topped) {
        return new ClimbRecord.Line(route, mode, topped, false, false, "5a", null);
    }

    private static ClimbRecord.Line top(String grade) {
        return new ClimbRecord.Line(
                UUID.randomUUID(), ClimbMode.TOP_ROPE, true, false, false, grade, null);
    }

    private static ClimbRecord.Line leadOn(String grade) {
        return new ClimbRecord.Line(
                UUID.randomUUID(), ClimbMode.LEAD, true, false, false, grade, null);
    }

    private static ClimbRecord.Line flash(UUID route, LocalDate openedOn) {
        return new ClimbRecord.Line(route, ClimbMode.LEAD, true, true, true, "6a", openedOn);
    }

    private static List<ClimbRecord.Line> tops(int count) {
        return IntStream.range(0, count).mapToObj(i -> top("4a")).toList();
    }

    private static List<ClimbRecord.Line> leads(int count) {
        return IntStream.range(0, count).mapToObj(i -> leadOn("4a")).toList();
    }

    private static List<ClimbRecord.Line> cleans(int count) {
        return IntStream.range(0, count)
                .mapToObj(
                        i ->
                                new ClimbRecord.Line(
                                        UUID.randomUUID(),
                                        ClimbMode.TOP_ROPE,
                                        true,
                                        true,
                                        false,
                                        "4a",
                                        null))
                .toList();
    }

    // --- awards ---

    private List<Award> awards() {
        return BadgeEngine.awards(history);
    }

    private List<BadgeCode> codes() {
        return awards().stream().map(Award::code).toList();
    }

    private long count(BadgeCode code) {
        return codes().stream().filter(code::equals).count();
    }

    private Award only(BadgeCode code) {
        var found = awards().stream().filter(award -> award.code() == code).toList();
        assertThat(found).hasSize(1);
        return found.getFirst();
    }

    static List<BadgeCode> toppedTiers() {
        return BadgeCode.tiers(BadgeFamily.TOPPED);
    }

    static List<BadgeCode> leadTiers() {
        return BadgeCode.tiers(BadgeFamily.LEAD);
    }

    static List<BadgeCode> dayTiers() {
        return BadgeCode.tiers(BadgeFamily.CLIMBING_DAYS);
    }
}
