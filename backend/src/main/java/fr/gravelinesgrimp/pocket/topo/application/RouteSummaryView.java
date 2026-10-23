package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStyle;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A route as listed in the topo, with the marks of the member who reads it ({@code favorite},
 * {@code checks}).
 */
public record RouteSummaryView(
        UUID id,
        String name,
        String grade,
        int gradeRank,
        int cordNumber,
        UUID sectorId,
        String sectorName,
        int sectorPosition,
        List<HoldView> holds,
        RouteStatus status,
        LocalDate openedOn,
        LocalDate plannedDismountOn,
        Instant dismountedAt,
        UUID photoId,
        List<RouteStyle> styles,
        int favoriteCount,
        int commentCount,
        int toppedCount,
        boolean favorite,
        List<ClimbMode> checks) {

    /** One hold colour of the route. */
    public record HoldView(UUID id, String label, String hex, boolean multicolor) {}

    /** Easiest first, then by name. */
    static final Comparator<RouteSummaryView> BY_GRADE =
            Comparator.comparingInt(RouteSummaryView::gradeRank)
                    .thenComparing(route -> route.name().toLowerCase(Locale.ROOT));

    /** Most recently dismounted first. */
    static final Comparator<RouteSummaryView> BY_DISMOUNT_DESCENDING =
            Comparator.comparing(
                            RouteSummaryView::dismountedAt,
                            Comparator.nullsLast(Comparator.<Instant>reverseOrder()))
                    .thenComparing(BY_GRADE);

    /** Soonest planned dismount first, undated ones last. */
    static final Comparator<RouteSummaryView> BY_PLANNED_DISMOUNT =
            Comparator.comparing(
                            RouteSummaryView::plannedDismountOn,
                            Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                    .thenComparing(BY_GRADE);
}
