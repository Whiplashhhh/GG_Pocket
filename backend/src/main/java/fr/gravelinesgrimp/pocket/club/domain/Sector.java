package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A part of the wall ("Grand dévers"…), holding cords. Never deleted: routes keep pointing to it.
 */
@Entity
@Table(name = "sector")
public class Sector extends TimestampedEntity {

    @Column(nullable = false, length = 60)
    private String name;

    /** Display order, 1 first. */
    @Column(nullable = false)
    private int position;

    @Column(length = 160)
    private String description;

    protected Sector() {
        // for JPA
    }

    public Sector(String name, String description, int position) {
        this.name = name.strip();
        this.description = blankToNull(description);
        this.position = position;
    }

    public void rename(String name, String description) {
        this.name = name.strip();
        this.description = blankToNull(description);
    }

    public void moveTo(int position) {
        this.position = position;
    }

    public String name() {
        return name;
    }

    public int position() {
        return position;
    }

    public String description() {
        return description;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
