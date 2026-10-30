package fr.gravelinesgrimp.pocket.badge.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * One time a badge was earned.
 *
 * @param key tells apart the times a repeatable badge was earned (the week, the month, the
 *     session…); empty for a tier, earned once. Stable while the history before it stays the same.
 * @param sessionId the session that earned it, shown in its feed card
 */
public record Award(BadgeCode code, String key, UUID sessionId, LocalDate earnedOn) {

    /** Identifies the award across recalculations. */
    public record Id(BadgeCode code, String key) {}

    public Id id() {
        return new Id(code, key);
    }
}
