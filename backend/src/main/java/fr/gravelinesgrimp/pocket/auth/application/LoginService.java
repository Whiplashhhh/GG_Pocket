package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** Checks a nickname and password. Opening the session itself is the API layer's job. */
@Service
public class LoginService {

    private final MemberRepository members;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    /** Compared against when the nickname is unknown, so both cases take the same time. */
    private final String unknownMemberHash;

    public LoginService(
            MemberRepository members, PasswordEncoder passwordEncoder, RateLimiter rateLimiter) {
        this.members = members;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.unknownMemberHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * @throws BusinessException {@code BAD_CREDENTIALS} whether the nickname or the password is
     *     wrong (never says which), {@code ACCOUNT_DISABLED} only to someone who knows the password
     */
    @Transactional
    public CurrentMemberView authenticate(String nickname, String password, String clientAddress) {
        rateLimiter.consume(RateLimiter.LOGIN_PER_ADDRESS, clientAddress);
        var failures = new Failures(Nickname.key(nickname), clientAddress);
        failures.ensureAllowed();

        // Not a possible nickname: no lookup (the database would lower-case it its own way).
        var found =
                Nickname.isValid(nickname.strip())
                        ? members.findByNicknameIgnoreCase(nickname.strip())
                        : Optional.<Member>empty();
        if (found.isEmpty()) {
            passwordEncoder.matches(password, unknownMemberHash);
            throw failures.badCredentials();
        }
        var member = found.get();
        if (!passwordEncoder.matches(password, member.passwordHash())) {
            throw failures.badCredentials();
        }
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "Disabled account");
        }
        if (passwordEncoder.upgradeEncoding(member.passwordHash())) {
            member.changePasswordHash(passwordEncoder.encode(password));
        }
        failures.reset();
        return CurrentMemberView.of(member);
    }

    /** Failed passwords, counted per nickname and address, and per nickname (DECISIONS D-050). */
    private final class Failures {

        private final String nicknameKey;
        private final String pairKey;

        Failures(String nicknameKey, String clientAddress) {
            this.nicknameKey = nicknameKey;
            this.pairKey = nicknameKey + "@" + clientAddress;
        }

        void ensureAllowed() {
            rateLimiter.ensureAllowed(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, pairKey);
            rateLimiter.ensureAllowed(RateLimiter.LOGIN_FAILURES_PER_NICKNAME, nicknameKey);
        }

        BusinessException badCredentials() {
            rateLimiter.record(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, pairKey);
            rateLimiter.record(RateLimiter.LOGIN_FAILURES_PER_NICKNAME, nicknameKey);
            return new BusinessException(ErrorCode.BAD_CREDENTIALS, "Bad credentials");
        }

        void reset() {
            rateLimiter.reset(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, pairKey);
        }
    }
}
