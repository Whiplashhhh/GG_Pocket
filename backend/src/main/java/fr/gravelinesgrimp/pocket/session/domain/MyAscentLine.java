package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.LocalDate;
import java.util.UUID;

/** One of my ascents, as the logbook reads it. */
public record MyAscentLine(
        UUID sessionId,
        LocalDate climbedOn,
        UUID routeId,
        ClimbMode mode,
        boolean topped,
        boolean clean,
        boolean pastHalf) {

    /** Not past the half (a top, or a try below the half). */
    public MyAscentLine(
            UUID sessionId,
            LocalDate climbedOn,
            UUID routeId,
            ClimbMode mode,
            boolean topped,
            boolean clean) {
        this(sessionId, climbedOn, routeId, mode, topped, clean, false);
    }
}
