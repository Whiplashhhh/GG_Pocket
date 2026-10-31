package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/** A badge a climber shows next to their name (D-106), in the order they chose. */
@Entity
@Table(name = "favorite_badge")
public class FavoriteBadge extends TimestampedEntity {

    /** Two or three badges next to a name: more would crowd the feed's cards. */
    public static final int MAX = 3;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 32)
    private Badge badge;

    @Column(nullable = false, updatable = false)
    private int slot;

    protected FavoriteBadge() {
        // for JPA
    }

    public FavoriteBadge(UUID memberId, Badge badge, int slot) {
        this.memberId = memberId;
        this.badge = badge;
        this.slot = slot;
    }

    public UUID memberId() {
        return memberId;
    }

    public Badge badge() {
        return badge;
    }

    public int slot() {
        return slot;
    }
}
