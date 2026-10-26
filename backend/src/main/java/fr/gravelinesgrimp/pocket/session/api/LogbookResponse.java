package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.LogbookView;
import fr.gravelinesgrimp.pocket.session.domain.AscentBest;
import fr.gravelinesgrimp.pocket.session.domain.Logbook;
import fr.gravelinesgrimp.pocket.topo.api.RouteSummaryResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "Logbook")
public record LogbookResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) Totals totals,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Hardest grade first, only grades with a topped route")
                List<GradeItem> grades,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "Every route I climbed, dismounted ones included; latest first")
                List<RouteItem> routes) {

    @Schema(
            name = "LogbookTotals",
            description =
                    "Ascent counters count repeats; routeCount counts distinct routes; maxGrade"
                            + " and maxLeadGrade: the hardest topped, in any mode and in lead.")
    public record Totals(
            @Schema(requiredMode = RequiredMode.REQUIRED) int sessionCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int climbingDays,
            @Schema(requiredMode = RequiredMode.REQUIRED) int ascentCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int leadToppedCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int cleanCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int routeCount,
            String maxGrade,
            Integer maxGradeRank,
            String maxLeadGrade,
            Integer maxLeadGradeRank) {}

    @Schema(
            name = "LogbookGrade",
            description = "Distinct routes topped at this grade, by the mode of their best ascent")
    public record GradeItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) String grade,
            @Schema(requiredMode = RequiredMode.REQUIRED) int gradeRank,
            @Schema(requiredMode = RequiredMode.REQUIRED) int topRope,
            @Schema(requiredMode = RequiredMode.REQUIRED) int lead) {}

    @Schema(name = "LogbookRoute")
    public record RouteItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
            @Schema(requiredMode = RequiredMode.REQUIRED) int ascentCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) AscentBest best,
            @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate firstClimbedOn,
            @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate lastClimbedOn) {}

    static LogbookResponse from(LogbookView view) {
        var totals = view.totals();
        var hardest = totals.hardestTopped();
        var hardestLead = totals.hardestLeadTopped();
        return new LogbookResponse(
                new Totals(
                        totals.sessionCount(),
                        totals.climbingDays(),
                        totals.ascentCount(),
                        totals.toppedCount(),
                        totals.leadToppedCount(),
                        totals.cleanCount(),
                        totals.routeCount(),
                        hardest.map(Logbook.Grade::code).orElse(null),
                        hardest.map(Logbook.Grade::rank).orElse(null),
                        hardestLead.map(Logbook.Grade::code).orElse(null),
                        hardestLead.map(Logbook.Grade::rank).orElse(null)),
                view.grades().stream()
                        .map(
                                count ->
                                        new GradeItem(
                                                count.grade().code(),
                                                count.grade().rank(),
                                                count.topRope(),
                                                count.lead()))
                        .toList(),
                view.routes().stream()
                        .map(
                                route ->
                                        new RouteItem(
                                                RouteSummaryResponse.from(route.route()),
                                                route.line().ascentCount(),
                                                route.line().toppedCount(),
                                                route.line().best(),
                                                route.line().firstClimbedOn(),
                                                route.line().lastClimbedOn()))
                        .toList());
    }
}
