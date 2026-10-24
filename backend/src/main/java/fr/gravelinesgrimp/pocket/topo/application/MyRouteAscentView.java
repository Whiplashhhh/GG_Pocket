package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.LocalDate;
import java.util.UUID;

/** One of my ascents of a route, for "mon historique" on the route sheet. */
public record MyRouteAscentView(
        UUID sessionId,
        LocalDate climbedOn,
        ClimbMode mode,
        boolean topped,
        boolean clean,
        int falls,
        boolean firstTry) {}
