package fr.gravelinesgrimp.pocket.session.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The month summary under the calendar (spec §8.6, §9.1): climbing days (two sessions on the same
 * day make one), sessions, topped ascents (repeats included), the hardest topped grade and the
 * average session feeling (one decimal, absent when no session was rated).
 */
public record MonthTotals(
        int climbingDays,
        int sessionCount,
        int toppedCount,
        Optional<SessionTotals.Line> hardestTopped,
        Optional<BigDecimal> averageFeeling) {

    /** One session of the month, reduced to what the summary needs. */
    public record Session(LocalDate climbedOn, Integer feeling, List<SessionTotals.Line> lines) {}

    public static MonthTotals of(List<Session> sessions) {
        var totals = SessionTotals.of(sessions.stream().flatMap(s -> s.lines().stream()).toList());
        var feelings = sessions.stream().map(Session::feeling).filter(Objects::nonNull).toList();
        var average =
                feelings.isEmpty()
                        ? Optional.<BigDecimal>empty()
                        : Optional.of(
                                BigDecimal.valueOf(feelings.stream().mapToInt(i -> i).sum())
                                        .divide(
                                                BigDecimal.valueOf(feelings.size()),
                                                1,
                                                RoundingMode.HALF_UP));
        return new MonthTotals(
                (int) sessions.stream().map(Session::climbedOn).distinct().count(),
                sessions.size(),
                totals.toppedCount(),
                totals.hardestTopped(),
                average);
    }
}
