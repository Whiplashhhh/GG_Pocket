package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.member.api.MediaUrls;
import fr.gravelinesgrimp.pocket.session.application.SessionView;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;
import fr.gravelinesgrimp.pocket.topo.api.RouteSummaryResponse;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "Session")
public record SessionResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "The climber") UUID memberId,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "The climber")
                MemberRefResponse author,
        @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate climbedOn,
        @Schema(description = "Out of 10") Integer feeling,
        String comment,
        String photoUrl,
        @Schema(requiredMode = RequiredMode.REQUIRED) SessionVisibility visibility,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant createdAt,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Mine, and its month is not frozen yet")
                boolean editable,
        @Schema(requiredMode = RequiredMode.REQUIRED) Summary summary,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<AscentItem> ascents,
        @Schema(requiredMode = RequiredMode.REQUIRED) SessionReactionsResponse reactions,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Deleted ones left out")
                long commentCount) {

    @Schema(name = "SessionSummary", description = "maxGrade: the hardest topped grade.")
    public record Summary(
            @Schema(requiredMode = RequiredMode.REQUIRED) int ascentCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int toppedCount,
            @Schema(requiredMode = RequiredMode.REQUIRED) int leadCount,
            String maxGrade,
            Integer maxGradeRank) {}

    @Schema(name = "Ascent")
    public record AscentItem(
            @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
            @Schema(requiredMode = RequiredMode.REQUIRED) RouteSummaryResponse route,
            @Schema(requiredMode = RequiredMode.REQUIRED) ClimbMode mode,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean topped,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean clean,
            @Schema(requiredMode = RequiredMode.REQUIRED) int falls,
            @Schema(requiredMode = RequiredMode.REQUIRED) boolean firstTry,
            @Schema(description = "Out of 10") Integer feeling,
            @Schema(description = "Who held the rope; absent when nobody was named")
                    MemberRefResponse belayer) {}

    static SessionResponse from(SessionView view) {
        var summary = view.summary();
        return new SessionResponse(
                view.id(),
                view.memberId(),
                MemberRefResponse.from(view.author()),
                view.climbedOn(),
                view.feeling(),
                view.comment(),
                MediaUrls.of(view.photoId()),
                view.visibility(),
                view.createdAt(),
                view.editable(),
                new Summary(
                        summary.ascentCount(),
                        summary.toppedCount(),
                        summary.leadCount(),
                        summary.maxGrade(),
                        summary.maxGradeRank()),
                view.ascents().stream()
                        .map(
                                ascent ->
                                        new AscentItem(
                                                ascent.id(),
                                                RouteSummaryResponse.from(ascent.route()),
                                                ascent.mode(),
                                                ascent.topped(),
                                                ascent.clean(),
                                                ascent.falls(),
                                                ascent.firstTry(),
                                                ascent.feeling(),
                                                ascent.belayer() == null
                                                        ? null
                                                        : MemberRefResponse.from(ascent.belayer())))
                        .toList(),
                SessionReactionsResponse.from(view.reactions()),
                view.commentCount());
    }
}
