package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A session as its reader sees it. {@code editable}: the reader owns it and its month is not frozen
 * yet.
 */
public record SessionView(
        UUID id,
        UUID memberId,
        LocalDate climbedOn,
        Integer feeling,
        String comment,
        UUID photoId,
        SessionVisibility visibility,
        Instant createdAt,
        boolean editable,
        SummaryView summary,
        List<AscentView> ascents) {

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
            Integer feeling) {}
}
