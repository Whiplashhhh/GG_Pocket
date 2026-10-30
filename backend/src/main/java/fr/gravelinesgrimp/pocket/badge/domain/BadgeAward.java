package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/** An {@link Award} as stored for a climber (spec §7 UserBadge). */
@Entity
@Table(name = "badge_award")
public class BadgeAward extends TimestampedEntity {

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 32)
    private BadgeCode code;

    @Column(name = "award_key", nullable = false, updatable = false, length = 64)
    private String awardKey;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "earned_on", nullable = false)
    private LocalDate earnedOn;

    protected BadgeAward() {
        // for JPA
    }

    public BadgeAward(UUID memberId, Award award) {
        this.memberId = memberId;
        this.code = award.code();
        this.awardKey = award.key();
        this.sessionId = award.sessionId();
        this.earnedOn = award.earnedOn();
    }

    /** The same award, earned again by another session. */
    public void moveTo(Award award) {
        this.sessionId = award.sessionId();
        this.earnedOn = award.earnedOn();
    }

    public Award toAward() {
        return new Award(code, awardKey, sessionId, earnedOn);
    }

    public UUID memberId() {
        return memberId;
    }

    public BadgeCode code() {
        return code;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public LocalDate earnedOn() {
        return earnedOn;
    }
}
