package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * A member saw that a route is in last chance ("J'accepte leurs destins", spec §8.9), at its
 * creation date. Removed when the route leaves last chance.
 */
@Entity
@Table(name = "last_chance_ack")
public class LastChanceAck extends TimestampedEntity {

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "route_id", nullable = false, updatable = false)
    private UUID routeId;

    protected LastChanceAck() {
        // for JPA
    }

    public LastChanceAck(UUID memberId, UUID routeId) {
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
