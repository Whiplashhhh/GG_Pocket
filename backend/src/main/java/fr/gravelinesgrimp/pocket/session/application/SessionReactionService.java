package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.infra.SessionReactionRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Reactions to sessions (spec §8.2): whoever reads someone else's session gives it one of the four
 * reactions, changes it or takes it back. Not to one's own session, like kudos (D-100).
 */
@Service
public class SessionReactionService {

    private final SessionReactionRepository reactions;
    private final ReadableSessions readable;
    private final SessionViews views;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public SessionReactionService(
            SessionReactionRepository reactions,
            ReadableSessions readable,
            SessionViews views,
            RateLimiter rateLimiter,
            Clock clock) {
        this.reactions = reactions;
        this.readable = readable;
        this.views = views;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read, {@code
     *     VALIDATION_FAILED} on my own session, {@code RATE_LIMITED}
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public SessionView.ReactionsView react(UUID memberId, UUID sessionId, ReactionKind kind) {
        var session = readable.load(memberId, sessionId);
        if (session.memberId().equals(memberId)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED, "No reaction to one's own session");
        }
        rateLimiter.consume(RateLimiter.SESSION_REACTION_PER_MEMBER, memberId.toString());
        reactions.upsert(UUID.randomUUID(), sessionId, memberId, kind.name(), clock.instant());
        return views.reactions(sessionId, memberId);
    }

    /** Nothing happens without a reaction to take back. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public SessionView.ReactionsView takeBack(UUID memberId, UUID sessionId) {
        readable.load(memberId, sessionId);
        reactions.deleteBySessionIdAndMemberId(sessionId, memberId);
        return views.reactions(sessionId, memberId);
    }
}
