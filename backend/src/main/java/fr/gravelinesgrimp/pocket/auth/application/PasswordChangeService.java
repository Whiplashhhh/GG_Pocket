package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.auth.domain.PasswordPolicy;
import fr.gravelinesgrimp.pocket.auth.infra.MemberSessionStore;
import fr.gravelinesgrimp.pocket.auth.infra.PasswordResetTokenRepository;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** A signed-in member changes their password, knowing the current one. */
@Service
public class PasswordChangeService {

    private final MemberRepository members;
    private final MemberSessionStore sessions;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public PasswordChangeService(
            MemberRepository members,
            MemberSessionStore sessions,
            PasswordResetTokenRepository resetTokens,
            PasswordPolicy passwordPolicy,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter) {
        this.members = members;
        this.sessions = sessions;
        this.resetTokens = resetTokens;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Wrong current passwords count like failed sign-ins from this address: a borrowed phone cannot
     * be used to guess it. Once committed, every other session of the member is closed ({@code
     * currentSessionId} stays open) and any pending reset link is cancelled.
     */
    @Transactional
    public void change(
            UUID memberId,
            String currentPassword,
            String newPassword,
            String currentSessionId,
            String clientAddress) {
        var member =
                members.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        var failureKey = Nickname.key(member.nickname()) + "@" + clientAddress;
        rateLimiter.ensureAllowed(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, failureKey);
        if (!passwordEncoder.matches(currentPassword, member.passwordHash())) {
            rateLimiter.record(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, failureKey);
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_WRONG, "Wrong current password");
        }
        passwordPolicy.check(newPassword);
        member.changePasswordHash(passwordEncoder.encode(newPassword));
        resetTokens.deleteUnusedOf(memberId);
        sessions.deleteAllOfAfterCommit(memberId, currentSessionId);
    }
}
