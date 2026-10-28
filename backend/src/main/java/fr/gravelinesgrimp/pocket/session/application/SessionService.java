package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.application.MediaService;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.session.domain.ClimbedRoute;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.RouteMode;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.infra.AscentRepository;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.social.application.Friendships;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sessions of a climber (spec §8.3, §9.3). The climber reads and changes them; their friends read
 * the ones shared with friends ({@link SessionAudience}). Every change locks the member row:
 * changes of one climber happen one at a time, so the "je sais la faire" bookkeeping and the replay
 * of the offline queue cannot race.
 */
@Service
public class SessionService {

    public static final int MAX_PAGE_SIZE = 30;

    /** {@code created} is false when the same {@code clientRef} was already saved. */
    public record Saved(SessionView session, boolean created) {}

    /** {@code nextCursor} is null on the last page. */
    public record SessionPage(List<SessionView> sessions, String nextCursor) {}

    private final ClimbingSessionRepository sessions;
    private final AscentRepository ascents;
    private final RouteRepository routes;
    private final MemberRepository members;
    private final OwnedSessions owned;
    private final SessionAudience audience;
    private final Friendships friendships;
    private final AutoRouteChecks autoChecks;
    private final SessionViews views;
    private final MediaService media;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public SessionService(
            ClimbingSessionRepository sessions,
            AscentRepository ascents,
            RouteRepository routes,
            MemberRepository members,
            OwnedSessions owned,
            SessionAudience audience,
            Friendships friendships,
            AutoRouteChecks autoChecks,
            SessionViews views,
            MediaService media,
            RateLimiter rateLimiter,
            Clock clock) {
        this.sessions = sessions;
        this.ascents = ascents;
        this.routes = routes;
        this.members = members;
        this.owned = owned;
        this.audience = audience;
        this.friendships = friendships;
        this.autoChecks = autoChecks;
        this.views = views;
        this.media = media;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * Saves a new session, or gives back the one already saved with this {@code clientRef}
     * (unchanged: the offline queue may send the same session twice).
     *
     * @throws BusinessException {@code VALIDATION_FAILED}, {@code INVALID_ASCENT}, {@code
     *     DATE_IN_THE_FUTURE}, {@code SESSION_TOO_OLD}, {@code MONTH_FROZEN}, {@code NOT_FOUND}
     *     (route), {@code ROUTE_DISMOUNTED}
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public Saved create(UUID memberId, UUID clientRef, SessionDraft draft) {
        lockMember(memberId);
        var existing = sessions.findByMemberIdAndClientRef(memberId, clientRef);
        if (existing.isPresent()) {
            return new Saved(views.of(existing.get(), memberId), false);
        }
        rateLimiter.consume(RateLimiter.SESSION_WRITE_PER_MEMBER, memberId.toString());
        draft.check();
        SessionCalendar.checkNewDate(draft.climbedOn(), clock.instant());
        checkRoutes(draft);
        var session = sessions.save(ClimbingSession.create(memberId, clientRef, draft));
        autoChecks.refresh(memberId, routeModes(session));
        return new Saved(views.of(session, memberId), true);
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public SessionView get(UUID readerId, UUID sessionId) {
        var session =
                sessions.findWithAscentsById(sessionId)
                        .filter(found -> audience.canRead(readerId, found))
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.NOT_FOUND, "No session"));
        return views.of(session, readerId);
    }

    /**
     * Replaces what the climber typed. A new date follows the rules of a new session.
     *
     * @throws BusinessException the ones of {@link #create}
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public SessionView update(UUID memberId, UUID sessionId, SessionDraft draft) {
        lockMember(memberId);
        var session = owned.load(memberId, sessionId);
        var now = clock.instant();
        SessionCalendar.checkNotFrozen(session.climbedOn(), now);
        rateLimiter.consume(RateLimiter.SESSION_WRITE_PER_MEMBER, memberId.toString());
        draft.check();
        if (!draft.climbedOn().equals(session.climbedOn())) {
            SessionCalendar.checkNewDate(draft.climbedOn(), now);
        }
        checkRoutes(draft);
        var touched = new HashSet<>(routeModes(session));
        session.update(draft);
        touched.addAll(routeModes(session));
        autoChecks.refresh(memberId, touched);
        return views.of(session, memberId);
    }

    /**
     * Deletes the session, its ascents and its photo.
     *
     * @throws BusinessException {@code NOT_FOUND}, {@code MONTH_FROZEN}
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public void delete(UUID memberId, UUID sessionId) {
        lockMember(memberId);
        var session = owned.load(memberId, sessionId);
        SessionCalendar.checkNotFrozen(session.climbedOn(), clock.instant());
        rateLimiter.consume(RateLimiter.SESSION_WRITE_PER_MEMBER, memberId.toString());
        var touched = routeModes(session);
        var photoId = session.photoId();
        sessions.delete(session);
        autoChecks.refresh(memberId, touched);
        if (photoId != null) {
            media.delete(photoId);
        }
    }

    /** My sessions, latest day first (then latest saved first). */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public SessionPage listMine(UUID memberId, String cursor, int size) {
        var limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        // One more than asked: tells whether another page exists.
        List<ClimbingSession> found;
        if (cursor == null || cursor.isBlank()) {
            found = sessions.findLatest(memberId, limit + 1);
        } else {
            var position = Cursor.decode(cursor);
            found =
                    sessions.findBefore(
                            memberId,
                            position.climbedOn(),
                            position.createdAt(),
                            position.id(),
                            limit + 1);
        }
        return page(found, limit, memberId);
    }

    /**
     * The sessions a climber shares with friends, as one of their friends reads them (their
     * profile, spec §8.8). Mine are all mine.
     *
     * @throws BusinessException {@code NOT_FOUND} (not a friend, or not an active member)
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public SessionPage listOf(UUID readerId, UUID climberId, String cursor, int size) {
        if (readerId.equals(climberId)) {
            return listMine(readerId, cursor, size);
        }
        audience.checkFriendOf(readerId, climberId);
        var limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        List<ClimbingSession> found;
        if (cursor == null || cursor.isBlank()) {
            found = sessions.findLatestShared(climberId, limit + 1);
        } else {
            var position = Cursor.decode(cursor);
            found =
                    sessions.findSharedBefore(
                            climberId,
                            position.climbedOn(),
                            position.createdAt(),
                            position.id(),
                            limit + 1);
        }
        return page(found, limit, readerId);
    }

    /**
     * The feed (spec §8.2): my sessions and the ones my friends share with friends, latest day
     * first. A friend whose account is disabled drops out, like everywhere else.
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public SessionPage feed(UUID readerId, String cursor, int size) {
        var limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        var climberIds = new ArrayList<UUID>();
        climberIds.add(readerId);
        members.findAllById(friendships.friendIdsOf(readerId)).stream()
                .filter(Member::isActive)
                .map(Member::id)
                .forEach(climberIds::add);
        List<ClimbingSession> found;
        if (cursor == null || cursor.isBlank()) {
            found = sessions.findLatestInFeed(readerId, climberIds, limit + 1);
        } else {
            var position = Cursor.decode(cursor);
            found =
                    sessions.findInFeedBefore(
                            readerId,
                            climberIds,
                            position.climbedOn(),
                            position.createdAt(),
                            position.id(),
                            limit + 1);
        }
        return page(found, limit, readerId);
    }

    /** {@code found} holds one more session than {@code limit} when another page exists. */
    private SessionPage page(List<ClimbingSession> found, int limit, UUID readerId) {
        var page = found.stream().limit(limit).toList();
        if (page.isEmpty()) {
            return new SessionPage(List.of(), null);
        }
        // Second query: the same sessions with their ascents, put back in page order.
        var withAscents =
                sessions
                        .findAllWithAscentsByIdIn(page.stream().map(ClimbingSession::id).toList())
                        .stream()
                        .collect(Collectors.toMap(ClimbingSession::id, session -> session));
        var ordered = page.stream().map(session -> withAscents.get(session.id())).toList();
        var last = page.getLast();
        var next =
                found.size() > limit
                        ? Cursor.encode(last.climbedOn(), last.createdAt(), last.id())
                        : null;
        return new SessionPage(views.of(ordered, readerId), next);
    }

    /**
     * Every route I climbed, the most recently climbed first (the quick picker, "premier essai").
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public List<ClimbedRoute> climbedRoutes(UUID memberId) {
        return ascents.findClimbedRoutes(memberId);
    }

    /**
     * Every route must exist. A dismounted one is fine only if it was still on the wall on the day
     * of the session (it may be logged up to a week later).
     */
    private void checkRoutes(SessionDraft draft) {
        var ids = draft.routeIds();
        var found = routes.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "No route");
        }
        for (var route : found) {
            if (route.isDismounted()
                    && LocalDate.ofInstant(route.dismountedAt(), ClubTime.ZONE)
                            .isBefore(draft.climbedOn())) {
                throw new BusinessException(
                        ErrorCode.ROUTE_DISMOUNTED, "Route " + route.id() + " was off");
            }
        }
    }

    private void lockMember(UUID memberId) {
        members.lockById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    private static Set<RouteMode> routeModes(ClimbingSession session) {
        return session.ascents().stream().map(Ascent::routeMode).collect(Collectors.toSet());
    }

    /** Opaque to clients: the day, saving date and id of the last session already shown. */
    record Cursor(LocalDate climbedOn, Instant createdAt, UUID id) {

        static String encode(LocalDate climbedOn, Instant createdAt, UUID id) {
            var raw = climbedOn + "|" + createdAt + "|" + id;
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String cursor) {
            try {
                var raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                var parts = raw.split("\\|", 3);
                return new Cursor(
                        LocalDate.parse(parts[0]),
                        Instant.parse(parts[1]),
                        UUID.fromString(parts[2]));
            } catch (RuntimeException e) {
                throw new BusinessException(ErrorCode.MALFORMED_REQUEST, "Bad session cursor");
            }
        }
    }
}
