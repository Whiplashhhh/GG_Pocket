package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * My logbook ("carnet", spec §8.8): everything I ever climbed, dismounted routes included (spec §2:
 * history is sacred). Counters count ascents, repeats included (§9.1); the grade pyramid counts
 * distinct routes, each in the mode of its best ascent.
 */
public record Logbook(Totals totals, List<GradeCount> grades, List<RouteLine> routes) {

    /** A grade, with its rank to compare. */
    public record Grade(String code, int rank) {}

    public record Totals(
            int sessionCount,
            int climbingDays,
            int ascentCount,
            int toppedCount,
            int leadToppedCount,
            int cleanCount,
            int routeCount,
            Optional<Grade> hardestTopped,
            Optional<Grade> hardestLeadTopped) {}

    /** Distinct routes topped at a grade, by the mode of their best ascent. */
    public record GradeCount(Grade grade, int topRope, int lead) {}

    public record RouteLine(
            UUID routeId,
            Grade grade,
            int ascentCount,
            int toppedCount,
            AscentBest best,
            LocalDate firstClimbedOn,
            LocalDate lastClimbedOn) {}

    /**
     * @param gradeOf the grade of each route of {@code ascents}
     */
    public static Logbook of(List<MyAscentLine> ascents, Function<UUID, Grade> gradeOf) {
        var topped = ascents.stream().filter(MyAscentLine::topped).toList();
        var routes =
                ascents.stream()
                        .collect(Collectors.groupingBy(MyAscentLine::routeId))
                        .entrySet()
                        .stream()
                        .map(
                                route ->
                                        line(
                                                route.getKey(),
                                                gradeOf.apply(route.getKey()),
                                                route.getValue()))
                        .sorted(
                                Comparator.comparing(RouteLine::lastClimbedOn)
                                        .thenComparingInt(line -> line.grade().rank())
                                        .reversed()
                                        .thenComparing(line -> line.routeId().toString()))
                        .toList();
        var totals =
                new Totals(
                        (int) ascents.stream().map(MyAscentLine::sessionId).distinct().count(),
                        (int) ascents.stream().map(MyAscentLine::climbedOn).distinct().count(),
                        ascents.size(),
                        topped.size(),
                        (int) topped.stream().filter(a -> a.mode() == ClimbMode.LEAD).count(),
                        (int) ascents.stream().filter(MyAscentLine::clean).count(),
                        routes.size(),
                        hardest(topped, gradeOf),
                        hardest(
                                topped.stream().filter(a -> a.mode() == ClimbMode.LEAD).toList(),
                                gradeOf));
        return new Logbook(totals, pyramid(routes), routes);
    }

    private static RouteLine line(UUID routeId, Grade grade, List<MyAscentLine> ascents) {
        var best =
                ascents.stream()
                        .map(a -> AscentBest.of(a.mode(), a.topped(), a.clean()))
                        .reduce(AscentBest.TRIED, AscentBest::bestWith);
        var days = ascents.stream().map(MyAscentLine::climbedOn).sorted().toList();
        return new RouteLine(
                routeId,
                grade,
                ascents.size(),
                (int) ascents.stream().filter(MyAscentLine::topped).count(),
                best,
                days.getFirst(),
                days.getLast());
    }

    private static Optional<Grade> hardest(
            List<MyAscentLine> ascents, Function<UUID, Grade> gradeOf) {
        return ascents.stream()
                .map(a -> gradeOf.apply(a.routeId()))
                .max(Comparator.comparingInt(Grade::rank));
    }

    private static List<GradeCount> pyramid(List<RouteLine> routes) {
        Map<Grade, List<RouteLine>> byGrade =
                routes.stream()
                        .filter(route -> route.best().topped())
                        .collect(Collectors.groupingBy(RouteLine::grade));
        return byGrade.entrySet().stream()
                .map(
                        grade -> {
                            var lead =
                                    (int)
                                            grade.getValue().stream()
                                                    .filter(route -> route.best().inLead())
                                                    .count();
                            return new GradeCount(
                                    grade.getKey(), grade.getValue().size() - lead, lead);
                        })
                .sorted(
                        Comparator.comparingInt((GradeCount count) -> count.grade().rank())
                                .reversed())
                .toList();
    }
}
