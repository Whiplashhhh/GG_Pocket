package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A session as its reader sees it. {@code editable}: the reader owns it and its month is not frozen
 * yet. {@code commentCount} leaves out the deleted comments.
 */
public record SessionView(
        UUID id,
        UUID memberId,
        MemberRefView author,
        LocalDate climbedOn,
        Integer feeling,
        String comment,
        UUID photoId,
        SessionVisibility visibility,
        Instant createdAt,
        boolean editable,
        SummaryView summary,
        List<AscentView> ascents,
        ReactionsView reactions,
        long commentCount) {

    /** The figures of the card; {@code maxGrade} is the hardest topped one. */
    public record SummaryView(
            int ascentCount,
            int toppedCount,
            int leadCount,
            String maxGrade,
            Integer maxGradeRank) {}

    public record AscentView(
            UUID id,
            RouteSummaryView route,
            ClimbMode mode,
            boolean topped,
            boolean clean,
            int falls,
            boolean firstTry,
            Integer feeling,
            MemberRefView belayer) {}

    /** One count per reaction, in the order of {@link ReactionKind}; {@code mine} may be null. */
    public record ReactionsView(List<ReactionCountView> counts, ReactionKind mine) {

        public static ReactionsView of(Map<ReactionKind, Long> counts, ReactionKind mine) {
            return new ReactionsView(
                    Arrays.stream(ReactionKind.values())
                            .map(
                                    kind ->
                                            new ReactionCountView(
                                                    kind, counts.getOrDefault(kind, 0L).intValue()))
                            .toList(),
                    mine);
        }
    }

    public record ReactionCountView(ReactionKind kind, int count) {}
}
