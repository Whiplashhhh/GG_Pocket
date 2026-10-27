package fr.gravelinesgrimp.pocket.social.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Two members and where they stand: a pending request, or friends (spec §7). There is one row per
 * pair whoever asked; declining, cancelling or ending the friendship deletes it.
 */
@Entity
@Table(name = "friendship")
public class Friendship extends TimestampedEntity {

    @Column(name = "requester_id", nullable = false, updatable = false)
    private UUID requesterId;

    @Column(name = "addressee_id", nullable = false, updatable = false)
    private UUID addresseeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FriendshipStatus status;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    protected Friendship() {
        // for JPA
    }

    private Friendship(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Nobody befriends themselves");
        }
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = FriendshipStatus.PENDING;
    }

    public static Friendship request(UUID requesterId, UUID addresseeId) {
        return new Friendship(requesterId, addresseeId);
    }

    /** Only the one who was asked accepts; accepting twice changes nothing. */
    public void acceptBy(UUID memberId, Instant at) {
        if (!addresseeId.equals(memberId)) {
            throw new IllegalStateException("Only the addressee accepts");
        }
        if (status == FriendshipStatus.PENDING) {
            status = FriendshipStatus.ACCEPTED;
            acceptedAt = at;
        }
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    /** The member on the other side of {@code memberId}, who must be one of the two. */
    public UUID otherThan(UUID memberId) {
        if (requesterId.equals(memberId)) {
            return addresseeId;
        }
        if (addresseeId.equals(memberId)) {
            return requesterId;
        }
        throw new IllegalArgumentException("Not part of this friendship");
    }

    public UUID requesterId() {
        return requesterId;
    }

    public UUID addresseeId() {
        return addresseeId;
    }

    public FriendshipStatus status() {
        return status;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }
}
