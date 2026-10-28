package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A comment on a session. Deleting it only hides it ({@link #deletedAt()}, {@link #deletedBy()}):
 * the board can still see what was moderated, and by whom.
 */
@Entity
@Table(name = "session_comment")
public class SessionComment extends TimestampedEntity {

    public static final int BODY_LENGTH = 500;

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "author_id", nullable = false, updatable = false)
    private UUID authorId;

    @Column(nullable = false, updatable = false, length = 500)
    private String body;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    protected SessionComment() {
        // for JPA
    }

    /** {@code body} already stripped and checked (1 to 500 characters). */
    public SessionComment(UUID sessionId, UUID authorId, String body) {
        this.sessionId = sessionId;
        this.authorId = authorId;
        this.body = body;
    }

    public void delete(UUID by, Instant at) {
        this.deletedBy = by;
        this.deletedAt = at;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isWrittenBy(UUID memberId) {
        return authorId.equals(memberId);
    }

    public UUID sessionId() {
        return sessionId;
    }

    public UUID authorId() {
        return authorId;
    }

    public String body() {
        return body;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public UUID deletedBy() {
        return deletedBy;
    }
}
