package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * One session of a climber as the badge engine reads it.
 *
 * @param savedAt orders the sessions of a same day
 */
public record ClimbRecord(
        UUID sessionId, LocalDate climbedOn, Instant savedAt, List<Line> ascents) {

    public ClimbRecord {
        ascents = List.copyOf(ascents);
    }

    /**
     * An ascent of the session.
     *
     * @param grade the route's grade code ("6a+")
     * @param routeOpenedOn null when the route has no opening date
     */
    public record Line(
            UUID routeId,
            ClimbMode mode,
            boolean topped,
            boolean clean,
            boolean firstTry,
            String grade,
            LocalDate routeOpenedOn) {}
}
