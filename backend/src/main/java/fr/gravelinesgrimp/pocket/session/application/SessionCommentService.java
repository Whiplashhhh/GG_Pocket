package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationEvent;
import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.SessionComment;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Comments on sessions (spec §8.2). Whoever reads a session comments on it; the author deletes
 * their comment, the board and the admin delete anyone's (moderation, spec §5), which goes to the
 * audit log. Deleting only hides a comment.
 */
@Service
public class SessionCommentService {

    /** A comment as shown to its reader, who may or may not delete it. */
    public record SessionCommentView(
            UUID id,
            MemberRefView author,
            String body,
            Instant createdAt,
            boolean deletable,
            ReplyToView replyTo) {}

    /** The comment answered and who wrote it (D-128); its text may be gone (deleted). */
    public record ReplyToView(UUID id, MemberRefView author) {}

    private final SessionCommentRepository comments;
    private final ClimbingSessionRepository sessions;
    private final ReadableSessions readable;
    private final MemberRepository members;
    private final RateLimiter rateLimiter;
    private final AuditLog auditLog;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SessionCommentService(
            SessionCommentRepository comments,
            ClimbingSessionRepository sessions,
            ReadableSessions readable,
            MemberRepository members,
            RateLimiter rateLimiter,
            AuditLog auditLog,
            ApplicationEventPublisher events,
            Clock clock) {
        this.comments = comments;
        this.sessions = sessions;
        this.readable = readable;
        this.members = members;
        this.rateLimiter = rateLimiter;
        this.auditLog = auditLog;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Oldest first, deleted ones left out.
     *
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<SessionCommentView> list(UUID readerId, UUID sessionId) {
        var reader = loadMember(readerId);
        readable.load(readerId, sessionId);
        var visible = comments.findVisibleBySessionId(sessionId);
        // The comments answered, deleted ones included: « en réponse à tom » stays true.
        Map<UUID, SessionComment> answered =
                comments
                        .findAllById(
                                visible.stream()
                                        .map(SessionComment::replyToId)
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(SessionComment::id, Function.identity()));
        Map<UUID, Member> authors =
                members
                        .findAllById(
                                Stream.concat(visible.stream(), answered.values().stream())
                                        .map(SessionComment::authorId)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        return visible.stream()
                .filter(comment -> authors.containsKey(comment.authorId()))
                .map(
                        comment ->
                                toView(
                                        comment,
                                        authors.get(comment.authorId()),
                                        reader,
                                        replyTo(comment, answered, authors)))
                .toList();
    }

    private static ReplyToView replyTo(
            SessionComment comment, Map<UUID, SessionComment> answered, Map<UUID, Member> authors) {
        var target = comment.replyToId() == null ? null : answered.get(comment.replyToId());
        var author = target == null ? null : authors.get(target.authorId());
        return author == null ? null : new ReplyToView(target.id(), MemberRefView.of(author));
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read, {@code
     *     VALIDATION_FAILED} for an empty or too long text, {@code RATE_LIMITED} beyond 40 comments
     *     an hour
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public SessionCommentView add(UUID authorId, UUID sessionId, String body) {
        return add(authorId, sessionId, body, null);
    }

    /**
     * A comment, or the answer to one ({@code replyTo}, D-128): a comment of the same session, not
     * deleted. Answering an answer is allowed; the app shows them all under the first comment.
     *
     * @throws BusinessException {@code NOT_FOUND} for a session I cannot read, {@code
     *     VALIDATION_FAILED} for an empty or too long text, or a comment to answer that is not one
     *     of the session (or deleted), {@code RATE_LIMITED} beyond 40 comments an hour
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public SessionCommentView add(UUID authorId, UUID sessionId, String body, UUID replyTo) {
        var author = loadMember(authorId);
        var session = readable.load(authorId, sessionId);
        var text = body == null ? "" : body.strip();
        if (text.isEmpty() || text.length() > SessionComment.BODY_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Comment empty or too long");
        }
        ReplyToView answering = null;
        UUID answeredAuthor = null;
        if (replyTo != null) {
            var target =
                    comments.findById(replyTo)
                            .filter(found -> found.sessionId().equals(sessionId))
                            .filter(found -> !found.isDeleted())
                            .orElseThrow(
                                    () ->
                                            new BusinessException(
                                                    ErrorCode.VALIDATION_FAILED,
                                                    "No such comment to answer"));
            answeredAuthor = target.authorId();
            answering =
                    members.findById(target.authorId())
                            .map(found -> new ReplyToView(target.id(), MemberRefView.of(found)))
                            .orElse(null);
        }
        rateLimiter.consume(RateLimiter.SESSION_COMMENT_PER_MEMBER, authorId.toString());
        var comment = comments.save(new SessionComment(sessionId, authorId, text, replyTo));
        // One ring per person: the one answered hears « answer », the climber « comment ».
        if (answeredAuthor != null && !answeredAuthor.equals(authorId)) {
            events.publishEvent(
                    NotificationEvent.aboutSession(
                            answeredAuthor,
                            NotificationKind.REPLY,
                            authorId,
                            sessionId,
                            session.climbedOn(),
                            null,
                            text));
        }
        if (!session.memberId().equals(authorId) && !session.memberId().equals(answeredAuthor)) {
            events.publishEvent(
                    NotificationEvent.aboutSession(
                            session.memberId(),
                            NotificationKind.COMMENT,
                            authorId,
                            sessionId,
                            session.climbedOn(),
                            null,
                            text));
        }
        return toView(comment, author, author, answering);
    }

    /**
     * The author deletes their comment, even on a session they no longer read (their words stay
     * theirs); the board and the admin delete anyone's, recorded in the audit log, from the
     * moderation list too.
     *
     * @throws BusinessException {@code NOT_FOUND} for a comment I neither wrote nor moderate
     */
    @PreAuthorize("hasRole('TOURIST')")
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
        // Read before the deletion, which erases the text (D-112).
        var text = comment.body();
        auditLog.record(
                actor,
                AuditAction.SESSION_COMMENT_DELETED,
                AuditTargetType.SESSION_COMMENT,
                comment.id(),
                label(comment),
                text,
                null);
        comment.delete(actorId, clock.instant());
    }

    /** "Julie, 2026-10-12 (Marco)": whose session, which day, whose comment. */
    private String label(SessionComment comment) {
        var session = sessions.findById(comment.sessionId()).orElseThrow();
        var climber = members.findById(session.memberId()).map(Member::nickname).orElse("?");
        var author = members.findById(comment.authorId()).map(Member::nickname).orElse("?");
        return climber + ", " + session.climbedOn() + " (" + author + ")";
    }

    private static SessionCommentView toView(
            SessionComment comment, Member author, Member reader, ReplyToView replyTo) {
        return new SessionCommentView(
                comment.id(),
                MemberRefView.of(author),
                comment.body(),
                comment.createdAt(),
                comment.isWrittenBy(reader.id()) || reader.role().isAtLeast(Role.BOARD),
                replyTo);
    }

    private Member loadMember(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
