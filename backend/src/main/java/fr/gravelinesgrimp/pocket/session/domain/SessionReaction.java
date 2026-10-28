package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * A member's reaction to someone else's session: one per member and session, changed in place
 * (written by {@code SessionReactionRepository#upsert}, which stays right when two arrive at once).
 */
@Entity
@Table(name = "session_reaction")
public class SessionReaction extends TimestampedEntity {

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReactionKind kind;

    protected SessionReaction() {
        // for JPA
    }

    public SessionReaction(UUID sessionId, UUID memberId, ReactionKind kind) {
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.kind = kind;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public UUID memberId() {
        return memberId;
    }

    public ReactionKind kind() {
        return kind;
    }
}
