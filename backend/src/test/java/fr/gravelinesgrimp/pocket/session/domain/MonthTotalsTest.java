package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.session.domain.MonthTotals.Session;
import fr.gravelinesgrimp.pocket.session.domain.SessionTotals.Line;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

class MonthTotalsTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 19);

    @Test
    void anEmptyMonthHasNothingToShow() {
        var totals = MonthTotals.of(List.of());
        assertThat(totals.climbingDays()).isZero();
        assertThat(totals.sessionCount()).isZero();
        assertThat(totals.toppedCount()).isZero();
        assertThat(totals.hardestTopped()).isEmpty();
        assertThat(totals.averageFeeling()).isEmpty();
    }

    @Test
    void twoSessionsOnTheSameDayMakeOneClimbingDay() {
        var totals =
                MonthTotals.of(
                        List.of(
                                new Session(MONDAY, 7, List.of(line(true, "6a", 20))),
                                new Session(MONDAY, null, List.of(line(true, "6a", 20))),
                                new Session(MONDAY.plusDays(2), 8, List.of())));
        assertThat(totals.climbingDays()).isEqualTo(2);
        assertThat(totals.sessionCount()).isEqualTo(3);
    }

    @Test
    void countsToppedAscentsWithRepeatsAndKeepsTheHardest() {
        var totals =
                MonthTotals.of(
                        List.of(
                                new Session(
                                        MONDAY,
                                        null,
                                        List.of(
                                                line(true, "6a", 20),
                                                line(true, "6a", 20),
                                                line(false, "7a", 31))),
                                new Session(
                                        MONDAY.plusDays(1), null, List.of(line(true, "6b+", 25)))));
        assertThat(totals.toppedCount()).isEqualTo(3);
        assertThat(totals.hardestTopped()).map(Line::grade).contains("6b+");
    }

    @Test
    void averagesTheRatedSessionsToOneDecimal() {
        var totals =
                MonthTotals.of(
                        List.of(
                                new Session(MONDAY, 7, List.of()),
                                new Session(MONDAY.plusDays(1), 8, List.of()),
                                new Session(MONDAY.plusDays(2), 8, List.of()),
                                new Session(MONDAY.plusDays(3), null, List.of())));
        assertThat(totals.averageFeeling()).contains(new BigDecimal("7.7"));
    }

    private static Line line(boolean topped, String grade, int rank) {
        return new Line(ClimbMode.TOP_ROPE, topped, grade, rank);
    }
}
