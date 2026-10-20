package fr.gravelinesgrimp.pocket.member.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A member of the club. Identity and profile only; the password hash is the one sign-in method of
 * v1 (passkeys will live in a table of their own).
 */
@Entity
@Table(name = "member")
public class Member extends TimestampedEntity {

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MemberStatus status;

    @Column(name = "avatar_id")
    private UUID avatarId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "show_age", nullable = false)
    private boolean showAge;

    @Column(name = "club_arrival_year")
    private Short clubArrivalYear;

    @Column(name = "climbing_since_year")
    private Short climbingSinceYear;

    @Column(length = 160)
    private String bio;

    @Column(name = "appears_in_rankings", nullable = false)
    private boolean appearsInRankings;

    @Column(name = "last_seen_version", length = 32)
    private String lastSeenVersion;

    @Column(name = "privacy_accepted_at", nullable = false)
    private Instant privacyAcceptedAt;

    @Column(name = "parental_consent_at")
    private Instant parentalConsentAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    protected Member() {
        // for JPA
    }

    /**
     * A new member, right after registration: privacy page accepted, parental consent recorded if
     * the member declared it (the caller checks when it is required).
     */
    public Member(
            MemberIdentity identity,
            String passwordHash,
            Role role,
            LocalDate birthDate,
            boolean parentalConsent,
            Instant now) {
        this.nickname = identity.nickname().strip();
        this.firstName = identity.firstName().strip();
        this.lastName = identity.lastName().strip();
        this.email = blankToNull(identity.email());
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
        this.birthDate = birthDate;
        this.showAge = false;
        this.appearsInRankings = true;
        this.privacyAcceptedAt = now;
        this.parentalConsentAt = parentalConsent ? now : null;
    }

    public void rename(MemberIdentity identity) {
        this.nickname = identity.nickname().strip();
        this.firstName = identity.firstName().strip();
        this.lastName = identity.lastName().strip();
        this.email = blankToNull(identity.email());
    }

    public void describe(MemberDetails details) {
        this.clubArrivalYear = toShort(details.clubArrivalYear());
        this.climbingSinceYear = toShort(details.climbingSinceYear());
        this.bio = blankToNull(details.bio());
        this.showAge = details.showAge();
        this.appearsInRankings = details.appearsInRankings();
    }

    /**
     * Sets the birth date. Declaring a parental consent records it once; it is kept even if the
     * birth date is later removed (it was given).
     */
    public void changeBirthDate(LocalDate birthDate, boolean parentalConsent, Instant now) {
        this.birthDate = birthDate;
        if (parentalConsent && parentalConsentAt == null) {
            this.parentalConsentAt = now;
        }
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void disable() {
        this.status = MemberStatus.DISABLED;
    }

    public void enable() {
        this.status = MemberStatus.ACTIVE;
    }

    public void changeAvatar(UUID avatarId) {
        this.avatarId = avatarId;
    }

    public void markSeen(Instant now) {
        this.lastSeenAt = now;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean hasParentalConsent() {
        return parentalConsentAt != null;
    }

    public String nickname() {
        return nickname;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public MemberStatus status() {
        return status;
    }

    public UUID avatarId() {
        return avatarId;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public boolean showAge() {
        return showAge;
    }

    public Integer clubArrivalYear() {
        return clubArrivalYear == null ? null : clubArrivalYear.intValue();
    }

    public Integer climbingSinceYear() {
        return climbingSinceYear == null ? null : climbingSinceYear.intValue();
    }

    public String bio() {
        return bio;
    }

    public boolean appearsInRankings() {
        return appearsInRankings;
    }

    public String lastSeenVersion() {
        return lastSeenVersion;
    }

    public Instant privacyAcceptedAt() {
        return privacyAcceptedAt;
    }

    public Instant parentalConsentAt() {
        return parentalConsentAt;
    }

    public Instant lastSeenAt() {
        return lastSeenAt;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
