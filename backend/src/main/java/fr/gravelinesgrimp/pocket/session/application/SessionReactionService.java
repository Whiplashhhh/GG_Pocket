package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.domain.SessionReaction;
import fr.gravelinesgrimp.pocket.session.infra.SessionReactionRepository;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reactions to sessions (spec §8.2): whoever reads someone else's session gives it one of the four
 * reactions, changes it or takes it back. Not to one's own session, like kudos (D-100).
 */
@Service
public class SessionReactionService {

    /** Who gave which reaction (D-128). */
    public record ReactorView(MemberRefView member, ReactionKind kind) {}

    private final SessionReactionRepository reactions;
    private final ReadableSessions readable;
    private final SessionViews views;
    private final MemberRepository members;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public SessionReactionService(
            SessionReactionRepository reactions,
            ReadableSessions readable,
            SessionViews views,
            MemberRepository members,
            RateLimiter rateLimiter,
            Clock clock) {
        this.reactions = reactions;
        this.readable = readable;
        this.views = views;
        this.members = members;
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

    /**
     * Who reacted, the latest first: whoever reads the session sees it, like the counts (club
     * feedback, 2026-11-16, D-128).
     *
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<ReactorView> list(UUID readerId, UUID sessionId) {
        readable.load(readerId, sessionId);
        var given = reactions.findAllBySessionIdOrderByUpdatedAtDesc(sessionId);
        Map<UUID, Member> reactors =
                members.findAllById(given.stream().map(SessionReaction::memberId).toList()).stream()
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        return given.stream()
                .filter(reaction -> reactors.containsKey(reaction.memberId()))
                .map(
                        reaction ->
                                new ReactorView(
                                        MemberRefView.of(reactors.get(reaction.memberId())),
                                        reaction.kind()))
                .toList();
    }
}
