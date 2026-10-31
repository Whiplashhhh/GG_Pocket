package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.member.application.ValidatedMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteComment;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

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
 * Comments on routes. Every member writes and deletes their own; the board and the admin delete
 * anyone's (moderation, spec §5), which goes to the audit log. Deleting only hides a comment.
 */
@Service
public class RouteCommentService {

    /** A comment as shown to its reader, who may or may not delete it. */
    public record RouteCommentView(
            UUID id, MemberRefView author, String body, Instant createdAt, boolean deletable) {}

    private final RouteCommentRepository comments;
    private final RouteRepository routes;
    private final MemberRepository members;
    private final RateLimiter rateLimiter;
    private final AuditLog auditLog;
    private final Clock clock;

    public RouteCommentService(
            RouteCommentRepository comments,
            RouteRepository routes,
            MemberRepository members,
            RateLimiter rateLimiter,
            AuditLog auditLog,
            Clock clock) {
        this.comments = comments;
        this.routes = routes;
        this.members = members;
        this.rateLimiter = rateLimiter;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    /** Oldest first, deleted ones left out; dismounted routes keep theirs. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteCommentView> list(UUID readerId, UUID routeId) {
        var reader = loadMember(readerId);
        loadRoute(routeId);
        var visible = comments.findVisibleByRouteId(routeId);
        Map<UUID, Member> authors =
                members
                        .findAllById(
                                visible.stream().map(RouteComment::authorId).distinct().toList())
                        .stream()
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        return visible.stream()
                .filter(comment -> authors.containsKey(comment.authorId()))
                .map(comment -> toView(comment, authors.get(comment.authorId()), reader))
                .toList();
    }

    /**
     * @throws BusinessException {@code ROUTE_DISMOUNTED} on a dismounted route, {@code
     *     VALIDATION_FAILED} for an empty or too long text, {@code RATE_LIMITED} beyond 20 comments
     *     an hour
     */
    @ValidatedMember
    @Transactional
    public RouteCommentView add(UUID authorId, UUID routeId, String body) {
        var author = loadMember(authorId);
        loadRoute(routeId).ensureOnTheWall();
        var text = body == null ? "" : body.strip();
        if (text.isEmpty() || text.length() > RouteComment.BODY_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Comment empty or too long");
        }
        rateLimiter.consume(RateLimiter.ROUTE_COMMENT_PER_MEMBER, authorId.toString());
        var comment = comments.save(new RouteComment(routeId, authorId, text));
        return toView(comment, author, author);
    }

    /**
     * The author deletes their comment; the board and the admin delete anyone's, recorded in the
     * audit log.
     *
     * @throws BusinessException {@code ACCESS_DENIED} for someone else's comment below BOARD
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
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED, "Only the board deletes someone else's comment");
        }
        comment.delete(actorId, clock.instant());
        var route = loadRoute(comment.routeId());
        var author = members.findById(comment.authorId()).map(Member::nickname).orElse("?");
        auditLog.record(
                actor,
                AuditAction.ROUTE_COMMENT_DELETED,
                AuditTargetType.ROUTE_COMMENT,
                comment.id(),
                route.name() + " (" + author + ")",
                comment.body(),
                null);
    }

    private static RouteCommentView toView(RouteComment comment, Member author, Member reader) {
        return new RouteCommentView(
                comment.id(),
                MemberRefView.of(author),
                comment.body(),
                comment.createdAt(),
                comment.isWrittenBy(reader.id()) || reader.role().isAtLeast(Role.BOARD));
    }

    private Route loadRoute(UUID routeId) {
        return routes.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No route"));
    }

    private Member loadMember(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
