package fr.gravelinesgrimp.pocket.member.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * The few columns read on every request to authorize a signed-in member.
 *
 * @param validated false while the board has not validated the account (D-107)
 */
public record MemberAccess(
        UUID id, Role role, MemberStatus status, boolean validated, Instant lastSeenAt) {

    /**
     * Authority of the validated accounts, on top of their role: only they reach the rest of the
     * club (directory, friends, podium, comments on routes). See {@code ValidatedMember}.
     */
    public static final String VALIDATED = "VALIDATED";

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
}
