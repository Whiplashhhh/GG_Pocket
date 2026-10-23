package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** One try of a route during a session ("ascension", spec §7). */
@Entity
@Table(name = "ascent")
public class Ascent extends TimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, updatable = false)
    private ClimbingSession session;

    @Column(nullable = false, updatable = false)
    private int position;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ClimbMode mode;

    @Column(nullable = false)
    private boolean topped;

    @Column(nullable = false)
    private boolean clean;

    @Column(nullable = false)
    private int falls;

    @Column(name = "first_try", nullable = false)
    private boolean firstTry;

    private Integer feeling;

    protected Ascent() {
        // for JPA
    }

    Ascent(ClimbingSession session, int position, AscentDraft draft) {
        this.session = session;
        this.position = position;
        update(draft);
    }

    void update(AscentDraft draft) {
        this.routeId = draft.routeId();
        this.mode = draft.mode();
        this.topped = draft.topped();
        this.clean = draft.clean();
        this.falls = draft.falls();
        this.firstTry = draft.firstTry();
        this.feeling = draft.feeling();
    }

    public int position() {
        return position;
    }

    public UUID routeId() {
        return routeId;
    }

    public ClimbMode mode() {
        return mode;
    }

    public boolean topped() {
        return topped;
    }

    public boolean clean() {
        return clean;
    }

    public int falls() {
        return falls;
    }

    public boolean firstTry() {
        return firstTry;
    }

    public Integer feeling() {
        return feeling;
    }

    /** The route and mode, for the "je sais la faire" bookkeeping. */
    public RouteMode routeMode() {
        return new RouteMode(routeId, mode);
    }
}
