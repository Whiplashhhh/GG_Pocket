package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * A day the gym is closed (D-116), set by the board: not a free session day, so the "Mois complet"
 * badge does not wait for it. Deleting it reopens the day.
 */
@Entity
@Table(name = "closed_day")
public class ClosedDay extends TimestampedEntity {

    public static final int MAX_LABEL_LENGTH = 60;

    @Column(name = "closed_on", nullable = false, unique = true)
    private LocalDate closedOn;

    /** "Vacances de Noël", or null. */
    @Column(length = MAX_LABEL_LENGTH)
    private String label;

    protected ClosedDay() {
        // for JPA
    }

    public ClosedDay(LocalDate closedOn, String label) {
        this.closedOn = closedOn;
        this.label = normalizeLabel(label);
    }

    public void relabel(String label) {
        this.label = normalizeLabel(label);
    }

    /** Blank means no label. */
    public static String normalizeLabel(String label) {
        return label == null || label.isBlank() ? null : label.strip();
    }

    public LocalDate closedOn() {
        return closedOn;
    }

    public String label() {
        return label;
    }
}
