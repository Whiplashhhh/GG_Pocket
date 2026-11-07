package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A French grade, 4a to 7c+. A fixed list, seeded by migration (DECISIONS D-074); the grades the
 * club dropped stay, marked retired (D-115).
 */
@Entity
@Table(name = "grade")
public class Grade extends TimestampedEntity {

    @Column(nullable = false, updatable = false, length = 4)
    private String code;

    /** 1 for the easiest grade: sorts and compares grades. */
    @Column(nullable = false, updatable = false)
    private int rank;

    /** A retired grade is no longer offered: no route has it any more (D-115). */
    @Column(nullable = false, updatable = false)
    private boolean retired;

    protected Grade() {
        // for JPA
    }

    public String code() {
        return code;
    }

    public int rank() {
        return rank;
    }

    public boolean retired() {
        return retired;
    }
}
