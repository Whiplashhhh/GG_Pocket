package fr.gravelinesgrimp.pocket.member.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
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

    /** A route of the topo, possibly dismounted (no association: members know nothing of it). */
    @Column(name = "favorite_route_id")
    private UUID favoriteRouteId;

    /** Null while the board has not validated the account (D-107). */
    @Column(name = "validated_at")
    private Instant validatedAt;

    /** Declared at sign-up; null for accounts that never went through it. */
    @Column(name = "declared_licensed")
    private Boolean declaredLicensed;

    /** Who a person not licensed is to the club; erased once the board has decided. */
    @Column(name = "join_note", length = LicenceDeclaration.NOTE_MAX)
    private String joinNote;

    protected Member() {
        // for JPA
    }

    /**
     * A validated member: the first admin, the demo accounts. Privacy page accepted, parental
     * consent recorded if declared (the caller checks when it is required).
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
        this.validatedAt = now;
    }

    /**
     * A new account from a club code (D-107): a tourist awaiting validation by the board, with what
     * they declared at sign-up.
     */
    public static Member awaitingValidation(
            MemberIdentity identity,
            String passwordHash,
            LocalDate birthDate,
            boolean parentalConsent,
            LicenceDeclaration declaration,
            Instant now) {
        var member =
                new Member(identity, passwordHash, Role.TOURIST, birthDate, parentalConsent, now);
        member.validatedAt = null;
        member.declaredLicensed = declaration.licensed();
        member.joinNote = declaration.joinNote();
        return member;
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

    /**
     * The board's decision on a new account: climber (licensed) or tourist. The join note has
     * served its purpose and is erased.
     *
     * @throws BusinessException {@code MEMBER_ALREADY_VALIDATED}, or {@code VALIDATION_FAILED} for
     *     a role other than tourist or climber
     */
    public void validate(Role role, Instant now) {
        if (isValidated()) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_VALIDATED, "Already validated");
        }
        if (role != Role.TOURIST && role != Role.CLIMBER) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED, "A new account becomes a tourist or a climber");
        }
        this.role = role;
        this.validatedAt = now;
        this.joinNote = null;
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

    /** The caller checks that the route exists; null clears it. */
    public void changeFavoriteRoute(UUID routeId) {
        this.favoriteRouteId = routeId;
    }

    public void markSeen(Instant now) {
        this.lastSeenAt = now;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isValidated() {
        return validatedAt != null;
    }

    /** Shown to the other members: in the directory, on profiles, as a friend (D-107). */
    public boolean isVisibleInClub() {
        return isActive() && isValidated();
    }

    public Boolean declaredLicensed() {
        return declaredLicensed;
    }

    public String joinNote() {
        return joinNote;
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

    public UUID favoriteRouteId() {
        return favoriteRouteId;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
