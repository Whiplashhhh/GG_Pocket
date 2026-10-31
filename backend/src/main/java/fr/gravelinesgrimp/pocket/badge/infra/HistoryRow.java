package fr.gravelinesgrimp.pocket.badge.infra;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A session and one of its ascents; the ascent fields are null for a session without ascents. */
public record HistoryRow(
        UUID sessionId,
        LocalDate climbedOn,
        Instant savedAt,
        UUID routeId,
        ClimbMode mode,
        Boolean topped,
        Boolean clean,
        Boolean firstTry,
        String grade,
        LocalDate routeOpenedOn) {}
