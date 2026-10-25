package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.application.PlanningView;
import fr.gravelinesgrimp.pocket.session.application.SessionView;
import fr.gravelinesgrimp.pocket.session.domain.MonthTotals;
import fr.gravelinesgrimp.pocket.session.domain.SessionTotals;
import fr.gravelinesgrimp.pocket.session.domain.Streak;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "Planning")
public record PlanningResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "2026-10") String month,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Its sessions can no longer change")
                boolean frozen,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Only the days with a session, earliest first")
                List<DayItem> days,
        @Schema(requiredMode = RequiredMode.REQUIRED) Totals totals,
        @Schema(requiredMode = RequiredMode.REQUIRED) StreakItem streak) {

    @Schema(name = "PlanningDay")
    public record DayItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate date,
            @Schema(requiredMode = RequiredMode.REQUIRED) List<SessionItem> sessions) {}

    @Schema(name = "PlanningSession")
    public record SessionItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
            @Schema(description = "Out of 10") Integer feeling,
            @Schema(requiredMode = RequiredMode.REQUIRED) SessionResponse.Summary summary) {}

    @Schema(
            name = "MonthTotals",
            description =
                    "climbingDays: days with a session (two sessions on a day make one);"
                            + " toppedCount: topped ascents, repeats included; maxGrade: the"
                            + " hardest topped; averageFeeling: of the rated sessions.")
    public record Totals(
            @Schema(requiredMode = RequiredMode.REQUIRED) int climbingDays,
            @Schema(requiredMode = RequiredMode.REQUIRED) int sessionCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
            String maxGrade,
            Integer maxGradeRank,
            @Schema(example = "7.5") BigDecimal averageFeeling) {}

    @Schema(
            name = "Streak",
            description =
                    "ISO weeks in a row with at least minDaysPerWeek climbing days, up to this"
                            + " week (which only counts once it has them).")
    public record StreakItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) int weeks,
            @Schema(requiredMode = RequiredMode.REQUIRED) int minDaysPerWeek,
            @Schema(requiredMode = RequiredMode.REQUIRED) int currentWeekDays) {}

    static PlanningResponse from(PlanningView view) {
        return new PlanningResponse(
                view.month().toString(),
                view.frozen(),
                view.days().stream()
                        .map(
                                day ->
                                        new DayItem(
                                                day.date(),
                                                day.sessions().stream()
                                                        .map(PlanningResponse::session)
                                                        .toList()))
                        .toList(),
                totals(view.totals()),
                streak(view.streak()));
    }

    private static SessionItem session(SessionView session) {
        var summary = session.summary();
        return new SessionItem(
                session.id(),
                session.feeling(),
                new SessionResponse.Summary(
                        summary.ascentCount(),
                        summary.toppedCount(),
                        summary.leadCount(),
                        summary.maxGrade(),
                        summary.maxGradeRank()));
    }

    private static Totals totals(MonthTotals totals) {
        var hardest = totals.hardestTopped();
        return new Totals(
                totals.climbingDays(),
                totals.sessionCount(),
                totals.toppedCount(),
                hardest.map(SessionTotals.Line::grade).orElse(null),
                hardest.map(SessionTotals.Line::gradeRank).orElse(null),
                totals.averageFeeling().orElse(null));
    }

    private static StreakItem streak(Streak streak) {
        return new StreakItem(streak.weeks(), streak.minDaysPerWeek(), streak.currentWeekDays());
    }
}
