package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A comment on a route. Deleting it only hides it ({@link #deletedAt()}, {@link #deletedBy()}): the
 * board can still see what was moderated, and by whom.
 */
@Entity
@Table(name = "route_comment")
public class RouteComment extends TimestampedEntity {

    public static final int BODY_LENGTH = 500;

    @Column(name = "route_id", nullable = false, updatable = false)
    private UUID routeId;

    @Column(name = "author_id", nullable = false, updatable = false)
    private UUID authorId;

    @Column(nullable = false, updatable = false, length = 500)
    private String body;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    protected RouteComment() {
        // for JPA
    }

    /** {@code body} already stripped and checked (1 to 500 characters). */
    public RouteComment(UUID routeId, UUID authorId, String body) {
        this.routeId = routeId;
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

    public UUID routeId() {
        return routeId;
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
