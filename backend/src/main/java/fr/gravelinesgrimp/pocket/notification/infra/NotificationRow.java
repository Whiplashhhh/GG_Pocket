package fr.gravelinesgrimp.pocket.notification.infra;

import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One event for the bell, straight from the tables. {@code sourceId} is the row it comes from
 * (reaction, comment, session or friendship); the session fields are null for a friendship.
 */
public record NotificationRow(
        NotificationKind kind,
        UUID sourceId,
        Instant at,
        UUID actorId,
        UUID sessionId,
        UUID sessionOwnerId,
        LocalDate climbedOn,
        String reaction,
        String excerpt) {}
