package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * A rope hanging on the wall, known by its painted number. Routes are set on a cord; the sector
 * shown for a route is the current sector of its cord, so moving a cord moves its routes.
 */
@Entity
@Table(name = "cord")
public class Cord extends TimestampedEntity {

    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 99;

    @Column(nullable = false)
    private int number;

    @Column(name = "sector_id", nullable = false)
    private UUID sectorId;

    /** An inactive cord keeps its routes but takes no new one. */
    @Column(nullable = false)
    private boolean active;

    protected Cord() {
        // for JPA
    }

    public Cord(int number, UUID sectorId, boolean active) {
        this.number = number;
        this.sectorId = sectorId;
        this.active = active;
    }

    public void update(int number, UUID sectorId, boolean active) {
        this.number = number;
        this.sectorId = sectorId;
        this.active = active;
    }

    public int number() {
        return number;
    }

    public UUID sectorId() {
        return sectorId;
    }

    public boolean active() {
        return active;
    }
}
