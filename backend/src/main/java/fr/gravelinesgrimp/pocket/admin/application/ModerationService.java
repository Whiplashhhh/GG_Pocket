package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionComment;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteComment;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The moderation list (spec §8.11): the latest comments on routes and on sessions together, newest
 * first, for the board and the admin. Deleting one goes through the service of its kind, which
 * writes the audit log.
 */
@Service
public class ModerationService {

    public static final int MAX_PAGE_SIZE = 100;

    public enum CommentKind {
        ROUTE,
        SESSION
    }

    /**
     * A comment with where it was written: {@code route} for a route, {@code climber} and {@code
     * climbedOn} for a session (the session itself stays its climber's and friends').
     */
    public record ModeratedCommentView(
            UUID id,
            CommentKind kind,
            MemberRefView author,
            String body,
            Instant createdAt,
            RouteRefView route,
            MemberRefView climber,
            LocalDate climbedOn) {}

    public record RouteRefView(UUID id, String name) {}

    /** {@code nextCursor} is null on the last page. */
    public record ModerationPage(List<ModeratedCommentView> comments, String nextCursor) {}

    /** Either kind, before its place is known. */
    private record Found(
            UUID id,
            CommentKind kind,
            UUID authorId,
            String body,
            Instant createdAt,
            UUID routeId,
            UUID sessionId) {

        static Found of(RouteComment comment) {
            return new Found(
                    comment.id(),
                    CommentKind.ROUTE,
                    comment.authorId(),
                    comment.body(),
                    comment.createdAt(),
                    comment.routeId(),
                    null);
        }

        static Found of(SessionComment comment) {
            return new Found(
                    comment.id(),
                    CommentKind.SESSION,
                    comment.authorId(),
                    comment.body(),
                    comment.createdAt(),
                    null,
                    comment.sessionId());
        }
    }

    /**
     * Newest first, then the order of PostgreSQL on uuids (the bytes, like their text), so that the
     * merged pages follow the cursor of both queries.
     */
    private static final Comparator<Found> NEWEST_FIRST =
            Comparator.comparing(Found::createdAt)
                    .thenComparing(found -> found.id().toString())
                    .reversed();

    private final RouteCommentRepository routeComments;
    private final SessionCommentRepository sessionComments;
    private final RouteRepository routes;
    private final ClimbingSessionRepository sessions;
    private final MemberRepository members;

    public ModerationService(
            RouteCommentRepository routeComments,
            SessionCommentRepository sessionComments,
            RouteRepository routes,
            ClimbingSessionRepository sessions,
            MemberRepository members) {
        this.routeComments = routeComments;
        this.sessionComments = sessionComments;
        this.routes = routes;
        this.sessions = sessions;
        this.members = members;
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional(readOnly = true)
    public ModerationPage latestComments(String cursor, int size) {
        var limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        // One more than asked from each kind: tells whether another page exists.
        var found = new ArrayList<Found>();
        if (cursor == null || cursor.isBlank()) {
            routeComments.findLatestVisible(limit + 1).forEach(c -> found.add(Found.of(c)));
            sessionComments.findLatestVisible(limit + 1).forEach(c -> found.add(Found.of(c)));
        } else {
            var position = Cursor.decode(cursor);
            routeComments
                    .findVisibleBefore(position.createdAt(), position.id(), limit + 1)
                    .forEach(c -> found.add(Found.of(c)));
            sessionComments
                    .findVisibleBefore(position.createdAt(), position.id(), limit + 1)
                    .forEach(c -> found.add(Found.of(c)));
        }
        found.sort(NEWEST_FIRST);
        var page = found.stream().limit(limit).toList();
        if (page.isEmpty()) {
            return new ModerationPage(List.of(), null);
        }
        var last = page.getLast();
        var next = found.size() > limit ? Cursor.encode(last.createdAt(), last.id()) : null;
        return new ModerationPage(views(page), next);
    }

    private List<ModeratedCommentView> views(List<Found> page) {
        Map<UUID, Route> routesById =
                ids(page, Found::routeId).isEmpty()
                        ? Map.of()
                        : routes.findAllById(ids(page, Found::routeId)).stream()
                                .collect(Collectors.toMap(Route::id, Function.identity()));
        Map<UUID, ClimbingSession> sessionsById =
                ids(page, Found::sessionId).isEmpty()
                        ? Map.of()
                        : sessions.findAllById(ids(page, Found::sessionId)).stream()
                                .collect(
                                        Collectors.toMap(ClimbingSession::id, Function.identity()));
        var memberIds = new HashSet<UUID>();
        page.forEach(found -> memberIds.add(found.authorId()));
        sessionsById.values().forEach(session -> memberIds.add(session.memberId()));
        Map<UUID, MemberRefView> refs =
                members.findAllById(memberIds).stream()
                        .collect(Collectors.toMap(Member::id, MemberRefView::of));
        // The foreign keys guarantee that every route, session and member exists.
        return page.stream()
                .map(
                        found -> {
                            var route =
                                    found.routeId() == null
                                            ? null
                                            : routesById.get(found.routeId());
                            var session =
                                    found.sessionId() == null
                                            ? null
                                            : sessionsById.get(found.sessionId());
                            return new ModeratedCommentView(
                                    found.id(),
                                    found.kind(),
                                    refs.get(found.authorId()),
                                    found.body(),
                                    found.createdAt(),
                                    route == null
                                            ? null
                                            : new RouteRefView(route.id(), route.name()),
                                    session == null ? null : refs.get(session.memberId()),
                                    session == null ? null : session.climbedOn());
                        })
                .toList();
    }

    private static List<UUID> ids(List<Found> page, Function<Found, UUID> id) {
        return page.stream().map(id).filter(Objects::nonNull).distinct().toList();
    }

    /** Opaque to clients: the date and id of the last comment already shown. */
    record Cursor(Instant createdAt, UUID id) {

        static String encode(Instant createdAt, UUID id) {
            var raw = createdAt + "|" + id;
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String cursor) {
            try {
                var raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                var parts = raw.split("\\|", 2);
                return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
            } catch (RuntimeException e) {
                throw new BusinessException(ErrorCode.MALFORMED_REQUEST, "Bad moderation cursor");
            }
        }
    }
}
