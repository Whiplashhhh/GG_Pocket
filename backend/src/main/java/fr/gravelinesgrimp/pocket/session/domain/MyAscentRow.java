package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.LocalDate;
import java.util.UUID;

/** One of my ascents of a route, with the session it belongs to. */
public record MyAscentRow(
        UUID sessionId,
        LocalDate climbedOn,
        ClimbMode mode,
        boolean topped,
        boolean clean,
        int falls,
        boolean firstTry) {}
