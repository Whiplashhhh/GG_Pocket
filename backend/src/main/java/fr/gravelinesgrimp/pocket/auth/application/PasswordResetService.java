package fr.gravelinesgrimp.pocket.auth.application;

import fr.gravelinesgrimp.pocket.auth.domain.PasswordPolicy;
import fr.gravelinesgrimp.pocket.auth.domain.PasswordResetToken;
import fr.gravelinesgrimp.pocket.auth.infra.MemberSessionStore;
import fr.gravelinesgrimp.pocket.auth.infra.PasswordResetTokenRepository;
import fr.gravelinesgrimp.pocket.member.application.CurrentMemberView;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberGovernance;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Password reset without e-mail (spec §6): the board creates a link, hands it over by any means,
 * and the member chooses a new password with it.
 */
@Service
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** The secret token goes into the link; the database only keeps its hash. */
    public record ResetLink(String token, Instant expiresAt) {}

    /** Who the link is for, so the screen can greet them and remind them of their login. */
    public record ResetTarget(String firstName, String nickname) {}

    private final PasswordResetTokenRepository tokens;
    private final MemberRepository members;
    private final MemberSessionStore sessions;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public PasswordResetService(
            PasswordResetTokenRepository tokens,
            MemberRepository members,
            MemberSessionStore sessions,
            PasswordPolicy passwordPolicy,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter,
            Clock clock) {
        this.tokens = tokens;
        this.members = members;
        this.sessions = sessions;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * Replaces any unused link of the member. Who may do this is decided by the caller (member
     * management checks the role matrix).
     */
    @Transactional
    public ResetLink createLink(UUID memberId, UUID createdBy) {
        tokens.deleteUnusedOf(memberId);
        var bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        var token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var stored =
                tokens.save(new PasswordResetToken(memberId, token, createdBy, clock.instant()));
        return new ResetLink(token, stored.expiresAt());
    }

    @Transactional(readOnly = true)
    public ResetTarget check(String token, String clientAddress) {
        rateLimiter.consume(RateLimiter.PASSWORD_RESET_PER_ADDRESS, clientAddress);
        var member =
                tokens.findByTokenHash(PasswordResetToken.hash(token))
                        .filter(found -> found.isUsable(clock.instant()))
                        .flatMap(
                                found ->
                                        members.findById(found.memberId())
                                                .filter(Member::isActive)
                                                .filter(
                                                        target ->
                                                                creatorStillManages(found, target)))
                        .orElseThrow(PasswordResetService::invalidLink);
        return new ResetTarget(member.firstName(), member.nickname());
    }

    /**
     * Sets the new password, uses the link, and closes every session of the member: whoever knew
     * the old password is out. The caller then opens a session on this device.
     */
    @Transactional
    public CurrentMemberView complete(String token, String newPassword, String clientAddress) {
        rateLimiter.consume(RateLimiter.PASSWORD_RESET_PER_ADDRESS, clientAddress);
        var now = clock.instant();
        var link =
                tokens.lockByTokenHash(PasswordResetToken.hash(token))
                        .filter(found -> found.isUsable(now))
                        .orElseThrow(PasswordResetService::invalidLink);
        var member =
                members.findById(link.memberId())
                        .filter(Member::isActive)
                        .filter(target -> creatorStillManages(link, target))
                        .orElseThrow(PasswordResetService::invalidLink);
        passwordPolicy.check(newPassword);
        member.changePasswordHash(passwordEncoder.encode(newPassword));
        link.markUsed(now);
        sessions.deleteAllOfAfterCommit(member.id(), null);
        return CurrentMemberView.of(member);
    }

    /**
     * Cancels the pending links of a member whose role or status just changed: a link made for a
     * climber must not open the account of the board member they became (DECISIONS D-052).
     */
    @Transactional
    public void cancelLinks(UUID memberId) {
        tokens.deleteUnusedOf(memberId);
    }

    /** The maker of the link must still be active and still allowed to manage this member. */
    private boolean creatorStillManages(PasswordResetToken link, Member target) {
        if (link.createdBy() == null) {
            return false;
        }
        return members.findById(link.createdBy())
                .filter(Member::isActive)
                .filter(
                        creator ->
                                MemberGovernance.canManageAccount(
                                        creator.role(),
                                        creator.id().equals(target.id()),
                                        target.role()))
                .isPresent();
    }

    private static BusinessException invalidLink() {
        return new BusinessException(
                ErrorCode.PASSWORD_RESET_LINK_INVALID, "Unknown, used or expired reset link");
    }
}
