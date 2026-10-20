package fr.gravelinesgrimp.pocket.member.domain;

import java.time.Instant;
import java.util.UUID;

/** The few columns read on every request to authorize a signed-in member. */
public record MemberAccess(UUID id, Role role, MemberStatus status, Instant lastSeenAt) {

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
}
