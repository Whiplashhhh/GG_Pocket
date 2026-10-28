package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.SessionComment;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Comments on sessions (spec §8.2). Whoever reads a session comments on it; the author deletes
 * their comment, the board and the admin delete anyone's (moderation, spec §5), which goes to the
 * audit log. Deleting only hides a comment.
 */
@Service
public class SessionCommentService {

    /** A comment as shown to its reader, who may or may not delete it. */
    public record SessionCommentView(
            UUID id, MemberRefView author, String body, Instant createdAt, boolean deletable) {}

    private final SessionCommentRepository comments;
    private final ClimbingSessionRepository sessions;
    private final ReadableSessions readable;
    private final MemberRepository members;
    private final RateLimiter rateLimiter;
    private final AuditLog auditLog;
    private final Clock clock;

    public SessionCommentService(
            SessionCommentRepository comments,
            ClimbingSessionRepository sessions,
            ReadableSessions readable,
            MemberRepository members,
            RateLimiter rateLimiter,
            AuditLog auditLog,
            Clock clock) {
        this.comments = comments;
        this.sessions = sessions;
        this.readable = readable;
        this.members = members;
        this.rateLimiter = rateLimiter;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    /**
     * Oldest first, deleted ones left out.
     *
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public List<SessionCommentView> list(UUID readerId, UUID sessionId) {
        var reader = loadMember(readerId);
        readable.load(readerId, sessionId);
        var visible = comments.findVisibleBySessionId(sessionId);
        Map<UUID, Member> authors =
                members
                        .findAllById(
                                visible.stream().map(SessionComment::authorId).distinct().toList())
                        .stream()
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        return visible.stream()
                .filter(comment -> authors.containsKey(comment.authorId()))
                .map(comment -> toView(comment, authors.get(comment.authorId()), reader))
                .toList();
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read, {@code
     *     VALIDATION_FAILED} for an empty or too long text, {@code RATE_LIMITED} beyond 40 comments
     *     an hour
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public SessionCommentView add(UUID authorId, UUID sessionId, String body) {
        var author = loadMember(authorId);
        readable.load(authorId, sessionId);
        var text = body == null ? "" : body.strip();
        if (text.isEmpty() || text.length() > SessionComment.BODY_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Comment empty or too long");
        }
        rateLimiter.consume(RateLimiter.SESSION_COMMENT_PER_MEMBER, authorId.toString());
        var comment = comments.save(new SessionComment(sessionId, authorId, text));
        return toView(comment, author, author);
    }

    /**
     * The author deletes their comment, even on a session they no longer read (their words stay
     * theirs); the board and the admin delete anyone's, recorded in the audit log, from the
     * moderation list too.
     *
     * @throws BusinessException {@code NOT_FOUND} for a comment I neither wrote nor moderate
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public void delete(UUID actorId, UUID commentId) {
        var actor = loadMember(actorId);
        var comment =
                comments.findById(commentId)
                        .filter(found -> !found.isDeleted())
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NOT_FOUND, "No comment"));
        if (comment.isWrittenBy(actorId)) {
            comment.delete(actorId, clock.instant());
            return;
        }
        if (!actor.role().isAtLeast(Role.BOARD)) {
            // Below the board, someone else's comment on a session is none of my business: as if
            // it did not exist (the session may be one I cannot read).
            throw new BusinessException(ErrorCode.NOT_FOUND, "No comment");
        }
        comment.delete(actorId, clock.instant());
        auditLog.record(
                actor,
                AuditAction.SESSION_COMMENT_DELETED,
                AuditTargetType.SESSION_COMMENT,
                comment.id(),
                label(comment),
                comment.body(),
                null);
    }

    /** "Julie, 2026-10-12 (Marco)": whose session, which day, whose comment. */
    private String label(SessionComment comment) {
        var session = sessions.findById(comment.sessionId()).orElseThrow();
        var climber = members.findById(session.memberId()).map(Member::nickname).orElse("?");
        var author = members.findById(comment.authorId()).map(Member::nickname).orElse("?");
        return climber + ", " + session.climbedOn() + " (" + author + ")";
    }

    private static SessionCommentView toView(SessionComment comment, Member author, Member reader) {
        return new SessionCommentView(
                comment.id(),
                MemberRefView.of(author),
                comment.body(),
                comment.createdAt(),
                comment.isWrittenBy(reader.id()) || reader.role().isAtLeast(Role.BOARD));
    }

    private Member loadMember(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
