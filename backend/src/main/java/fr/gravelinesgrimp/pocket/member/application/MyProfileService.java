package fr.gravelinesgrimp.pocket.member.application;

import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/** A member reads and edits their own profile. */
@Service
public class MyProfileService {

    private final MemberRepository members;
    private final UniqueMemberFields uniqueFields;
    private final Clock clock;

    public MyProfileService(
            MemberRepository members, UniqueMemberFields uniqueFields, Clock clock) {
        this.members = members;
        this.uniqueFields = uniqueFields;
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
        var today = ClubTime.today(clock);
        checkYearNotInFuture(change.details().clubArrivalYear(), today.getYear());
        checkYearNotInFuture(change.details().climbingSinceYear(), today.getYear());
        if (Age.needsParentalConsent(change.birthDate(), today)
                && !member.hasParentalConsent()
                && !change.parentalConsent()) {
            throw new BusinessException(
                    ErrorCode.PARENTAL_CONSENT_REQUIRED, "Under 15 without parental consent");
        }
        uniqueFields.check(change.identity().nickname(), change.identity().email(), memberId);

        member.rename(change.identity());
        member.describe(change.details());
        member.changeBirthDate(change.birthDate(), change.parentalConsent(), clock.instant());
        try {
            members.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw UniqueMemberFields.translate(e);
        }
        return MyProfileView.of(member, today);
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
