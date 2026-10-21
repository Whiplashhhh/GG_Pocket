package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/** "Je sais la faire": a member can climb a route in a mode. One per member, route and mode. */
@Entity
@Table(name = "route_check")
public class RouteCheck extends TimestampedEntity {

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "route_id", nullable = false, updatable = false)
    private UUID routeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private ClimbMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CheckSource source;

    protected RouteCheck() {
        // for JPA
    }

    public RouteCheck(UUID memberId, UUID routeId, ClimbMode mode, CheckSource source) {
        this.memberId = memberId;
        this.routeId = routeId;
        this.mode = mode;
        this.source = source;
    }

    public UUID memberId() {
        return memberId;
    }

    public UUID routeId() {
        return routeId;
    }

    public ClimbMode mode() {
        return mode;
    }

    public CheckSource source() {
        return source;
    }
}
