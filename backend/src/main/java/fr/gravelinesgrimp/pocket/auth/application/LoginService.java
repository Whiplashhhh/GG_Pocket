package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var nicknameKey = Nickname.key(nickname);
        rateLimiter.ensureAllowed(RateLimiter.LOGIN_FAILURES_PER_NICKNAME, nicknameKey);

        var found = members.findByNicknameIgnoreCase(nickname.strip());
        if (found.isEmpty()) {
            passwordEncoder.matches(password, unknownMemberHash);
            throw badCredentials(nicknameKey);
        }
        var member = found.get();
        if (!passwordEncoder.matches(password, member.passwordHash())) {
            throw badCredentials(nicknameKey);
        }
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "Disabled account");
        }
        if (passwordEncoder.upgradeEncoding(member.passwordHash())) {
            member.changePasswordHash(passwordEncoder.encode(password));
        }
        rateLimiter.reset(RateLimiter.LOGIN_FAILURES_PER_NICKNAME, nicknameKey);
        return CurrentMemberView.of(member);
    }

    private BusinessException badCredentials(String nicknameKey) {
        rateLimiter.record(RateLimiter.LOGIN_FAILURES_PER_NICKNAME, nicknameKey);
        return new BusinessException(ErrorCode.BAD_CREDENTIALS, "Bad credentials");
    }
}
