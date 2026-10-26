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
        boolean clean) {}
