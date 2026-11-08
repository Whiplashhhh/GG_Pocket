package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberDetails;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/** A member reads and edits their own profile. */
@Service
public class MyProfileService {

    private final MemberRepository members;
    private final UniqueMemberFields uniqueFields;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public MyProfileService(
            MemberRepository members,
            UniqueMemberFields uniqueFields,
            RateLimiter rateLimiter,
            Clock clock) {
        this.members = members;
        this.uniqueFields = uniqueFields;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MyProfileView get(UUID memberId) {
        return MyProfileView.of(load(memberId), ClubTime.today(clock));
    }

    @Transactional(readOnly = true)
    public CurrentMemberView current(UUID memberId) {
        return CurrentMemberView.of(load(memberId));
    }

    @Transactional
    public MyProfileView update(UUID memberId, ProfileChange change) {
        var member = load(memberId);
        rateLimiter.consume(RateLimiter.PROFILE_UPDATE_PER_MEMBER, memberId.toString());
        var today = ClubTime.today(clock);
        checkYearNotInFuture(change.details().clubArrivalYear(), today.getYear());
        checkYearNotInFuture(change.details().climbingSinceYear(), today.getYear());
        checkParentalConsent(member, change.birthDate(), change.parentalConsent(), today);
        uniqueFields.check(change.identity().nickname(), change.identity().email(), memberId);

        member.rename(change.identity());
        member.describe(adultsOnlyShowAge(change, today));
        member.changeBirthDate(change.birthDate(), change.parentalConsent(), clock.instant());
        try {
            members.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw UniqueMemberFields.translate(e);
        }
        return MyProfileView.of(member, today);
    }

    /**
     * The birth date an older account never gave (D-119), with the same parental consent rule as at
     * registration.
     *
     * @throws BusinessException {@code PARENTAL_CONSENT_REQUIRED} under 15 without consent
     */
    @Transactional
    public CurrentMemberView setBirthDate(
            UUID memberId, LocalDate birthDate, boolean parentalConsent) {
        var member = load(memberId);
        rateLimiter.consume(RateLimiter.PROFILE_UPDATE_PER_MEMBER, memberId.toString());
        checkParentalConsent(member, birthDate, parentalConsent, ClubTime.today(clock));
        member.changeBirthDate(birthDate, parentalConsent, clock.instant());
        return CurrentMemberView.of(member);
    }

    /**
     * "Je participe au fil du club" (D-120), by the member themself only. Leaving takes their
     * sessions shared with the club out of it at once (read when reading, nothing rewritten). An
     * account awaiting validation does not take part (D-107).
     */
    @ValidatedMember
    @Transactional
    public CurrentMemberView changeClubFeed(UUID memberId, boolean inClubFeed) {
        var member = load(memberId);
        rateLimiter.consume(RateLimiter.PROFILE_UPDATE_PER_MEMBER, memberId.toString());
        member.changeClubFeed(inClubFeed);
        return CurrentMemberView.of(member);
    }

    /** "Afficher mon âge" is offered to adults only: a minor's box stays unchecked (D-119). */
    private static MemberDetails adultsOnlyShowAge(ProfileChange change, LocalDate today) {
        var details = change.details();
        if (!details.showAge() || !Age.isMinor(change.birthDate(), today)) {
            return details;
        }
        return new MemberDetails(
                details.clubArrivalYear(),
                details.climbingSinceYear(),
                details.bio(),
                false,
                details.appearsInRankings());
    }

    private static void checkParentalConsent(
            Member member, LocalDate birthDate, boolean parentalConsent, LocalDate today) {
        if (Age.needsParentalConsent(birthDate, today)
                && !member.hasParentalConsent()
                && !parentalConsent) {
            throw new BusinessException(
                    ErrorCode.PARENTAL_CONSENT_REQUIRED, "Under 15 without parental consent");
        }
    }

    private Member load(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.NOT_FOUND, "No member " + memberId));
    }

    private static void checkYearNotInFuture(Integer year, int currentYear) {
        if (year != null && year > currentYear) {
            throw new BusinessException(ErrorCode.YEAR_IN_THE_FUTURE, "Year " + year);
        }
    }
}
