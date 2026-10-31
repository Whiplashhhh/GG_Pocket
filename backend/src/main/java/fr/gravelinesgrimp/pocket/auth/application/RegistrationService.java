package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.auth.domain.PasswordPolicy;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCodeFormat;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.application.UniqueMemberFields;
import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.LicenceDeclaration;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Joining the club with a registration code. */
@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationCodeRepository codes;
    private final MemberRepository members;
    private final UniqueMemberFields uniqueFields;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public RegistrationService(
            RegistrationCodeRepository codes,
            MemberRepository members,
            UniqueMemberFields uniqueFields,
            PasswordPolicy passwordPolicy,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter,
            Clock clock) {
        this.codes = codes;
        this.members = members;
        this.uniqueFields = uniqueFields;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /** First step of the registration screen: is this code worth filling the form for? */
    @Transactional(readOnly = true)
    public void checkCode(String typedCode, String clientAddress) {
        rateLimiter.consume(RateLimiter.REGISTRATION_CODE_PER_ADDRESS, clientAddress);
        codes.findByCode(RegistrationCodeFormat.normalize(typedCode))
                .filter(code -> code.isUsable(clock.instant()))
                .orElseThrow(RegistrationService::invalidCode);
    }

    /** Creates the member and uses the code once. The caller then opens the session. */
    @Transactional
    public CurrentMemberView register(Registration registration, String clientAddress) {
        rateLimiter.consume(RateLimiter.REGISTRATION_PER_ADDRESS, clientAddress);
        var now = clock.instant();
        RegistrationCode code =
                codes.lockByCode(RegistrationCodeFormat.normalize(registration.code()))
                        .filter(found -> found.isUsable(now))
                        .orElseThrow(RegistrationService::invalidCode);
        passwordPolicy.check(registration.password());
        var needsConsent =
                Age.needsParentalConsent(registration.birthDate(), ClubTime.today(clock));
        if (needsConsent && !registration.parentalConsent()) {
            throw new BusinessException(
                    ErrorCode.PARENTAL_CONSENT_REQUIRED, "Under 15 without parental consent");
        }
        var declaration = LicenceDeclaration.of(registration.licensed(), registration.joinNote());
        var identity = registration.identity();
        uniqueFields.check(identity.nickname(), identity.email(), UniqueMemberFields.NOBODY);

        var passwordHash = passwordEncoder.encode(registration.password());
        // The first-start code makes the president, validated at once; club codes make tourists
        // awaiting validation by the board (D-107).
        var member =
                code.grantedRole() == Role.ADMIN
                        ? new Member(
                                identity,
                                passwordHash,
                                Role.ADMIN,
                                registration.birthDate(),
                                needsConsent,
                                now)
                        : Member.awaitingValidation(
                                identity,
                                passwordHash,
                                registration.birthDate(),
                                needsConsent,
                                declaration,
                                now);
        code.recordUse();
        try {
            members.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw UniqueMemberFields.translate(e);
        }
        if (code.grantedRole() == Role.ADMIN) {
            log.info("First admin registered with the first-start code: {}", member.id());
        }
        return CurrentMemberView.of(member);
    }

    private static BusinessException invalidCode() {
        return new BusinessException(
                ErrorCode.REGISTRATION_CODE_INVALID, "Unknown, inactive, expired or used up code");
    }
}
