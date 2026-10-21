package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/** A heart ("coup de cœur") of a member on a route, dated by its creation. See FavoriteLimit. */
@Entity
@Table(name = "favorite")
public class Favorite extends TimestampedEntity {

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "route_id", nullable = false, updatable = false)
    private UUID routeId;

    protected Favorite() {
        // for JPA
    }

    public Favorite(UUID memberId, UUID routeId) {
        this.memberId = memberId;
        this.routeId = routeId;
    }

    public UUID memberId() {
        return memberId;
    }

    public UUID routeId() {
        return routeId;
    }
}
