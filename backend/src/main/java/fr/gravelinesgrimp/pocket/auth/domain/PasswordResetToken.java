package fr.gravelinesgrimp.pocket.auth.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A single-use password reset link, valid 24 hours, created by the board for a member (no e-mail in
 * v1, spec §6). Only the SHA-256 of the token is stored.
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken extends TimestampedEntity {

    public static final Duration VALIDITY = Duration.ofHours(24);

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected PasswordResetToken() {
        // for JPA
    }

    public PasswordResetToken(UUID memberId, String token, UUID createdBy, Instant now) {
        this.memberId = memberId;
        this.tokenHash = hash(token);
        this.expiresAt = now.plus(VALIDITY);
        this.createdBy = createdBy;
    }

    public static String hash(String token) {
        return Sha256.hex(token);
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void markUsed(Instant now) {
        this.usedAt = now;
    }

    public UUID memberId() {
        return memberId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant usedAt() {
        return usedAt;
    }
}
