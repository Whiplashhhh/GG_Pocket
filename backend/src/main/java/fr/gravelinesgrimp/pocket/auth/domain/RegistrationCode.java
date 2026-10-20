package fr.gravelinesgrimp.pocket.auth.domain;

import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A code given by the club (shown at the gym) that opens registration. Stored in its normalized
 * form (see {@link RegistrationCodeFormat}). Codes are deactivated, never deleted, so the number of
 * registrations they brought stays known.
 */
@Entity
@Table(name = "registration_code")
public class RegistrationCode extends TimestampedEntity {

    @Column(nullable = false, updatable = false, length = 16)
    private String code;

    @Column(nullable = false, length = 80)
    private String label;

    /** CLIMBER for club codes; ADMIN only for the first-start code. */
    @Enumerated(EnumType.STRING)
    @Column(name = "granted_role", nullable = false, updatable = false, length = 16)
    private Role grantedRole;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "use_count", nullable = false)
    private int useCount;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    protected RegistrationCode() {
        // for JPA
    }

    public RegistrationCode(
            String code,
            String label,
            Role grantedRole,
            Instant expiresAt,
            Integer maxUses,
            UUID createdBy) {
        this.code = code;
        this.label = label.strip();
        this.grantedRole = grantedRole;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.useCount = 0;
        this.active = true;
        this.createdBy = createdBy;
    }

    /** Where a code stands, as shown to the admin. */
    public enum State {
        USABLE,
        INACTIVE,
        EXPIRED,
        EXHAUSTED
    }

    public State state(Instant now) {
        if (!active) {
            return State.INACTIVE;
        }
        if (expiresAt != null && !now.isBefore(expiresAt)) {
            return State.EXPIRED;
        }
        if (maxUses != null && useCount >= maxUses) {
            return State.EXHAUSTED;
        }
        return State.USABLE;
    }

    public boolean isUsable(Instant now) {
        return state(now) == State.USABLE;
    }

    public void recordUse() {
        this.useCount++;
    }

    public void update(String label, Instant expiresAt, Integer maxUses, boolean active) {
        this.label = label.strip();
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public Role grantedRole() {
        return grantedRole;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Integer maxUses() {
        return maxUses;
    }

    public int useCount() {
        return useCount;
    }

    public boolean active() {
        return active;
    }
}
