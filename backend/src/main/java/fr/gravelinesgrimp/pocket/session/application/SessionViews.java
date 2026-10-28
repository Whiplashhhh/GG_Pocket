package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.ReactionCount;
import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;
import fr.gravelinesgrimp.pocket.session.domain.SessionCount;
import fr.gravelinesgrimp.pocket.session.domain.SessionReaction;
import fr.gravelinesgrimp.pocket.session.domain.SessionTotals;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionReactionRepository;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.application.RouteViews;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds session views in a fixed number of queries whatever the number of sessions: the sessions
 * come with their ascents, and the routes, climbers, reactions and comment counts of all of them
 * are read in one go each.
 */
@Component
public class SessionViews {

    private final RouteRepository routes;
    private final RouteViews routeViews;
    private final MemberRepository members;
    private final SessionReactionRepository reactions;
    private final SessionCommentRepository comments;
    private final Clock clock;

    public SessionViews(
            RouteRepository routes,
            RouteViews routeViews,
            MemberRepository members,
            SessionReactionRepository reactions,
            SessionCommentRepository comments,
            Clock clock) {
        this.routes = routes;
        this.routeViews = routeViews;
        this.members = members;
        this.reactions = reactions;
        this.comments = comments;
        this.clock = clock;
    }

    /** The reactions to one session as {@code readerId} sees them (after reacting). */
    @Transactional(propagation = Propagation.MANDATORY)
    public SessionView.ReactionsView reactions(UUID sessionId, UUID readerId) {
        return reactionsOf(List.of(sessionId), readerId)
                .getOrDefault(sessionId, SessionView.ReactionsView.of(Map.of(), null));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SessionView of(ClimbingSession session, UUID readerId) {
        return of(List.of(session), readerId).getFirst();
    }

    /** Views in the order of {@code sessions}. */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<SessionView> of(List<ClimbingSession> sessions, UUID readerId) {
        if (sessions.isEmpty()) {
            return List.of();
        }
        var sessionIds = sessions.stream().map(ClimbingSession::id).toList();
        Map<UUID, MemberRefView> authors =
                members
                        .findAllById(
                                sessions.stream()
                                        .map(ClimbingSession::memberId)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Member::id, MemberRefView::of));
        var reactionsBySession = reactionsOf(sessionIds, readerId);
        Map<UUID, Long> commentCounts =
                comments.countVisiblePerSession(sessionIds).stream()
                        .collect(Collectors.toMap(SessionCount::sessionId, SessionCount::count));
        var routeIds =
                sessions.stream()
                        .flatMap(session -> session.ascents().stream())
                        .map(Ascent::routeId)
                        .collect(Collectors.toSet());
        Map<UUID, RouteSummaryView> summaries =
                routeIds.isEmpty()
                        ? Map.of()
                        : routeViews
                                .summaries(routes.findAllWithStylesByIdIn(routeIds), readerId)
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                RouteSummaryView::id, Function.identity()));
        var now = clock.instant();
        var noReaction = SessionView.ReactionsView.of(Map.of(), null);
        return sessions.stream()
                .map(
                        session ->
                                view(
                                        session,
                                        summaries,
                                        // The foreign key guarantees that the climber exists.
                                        authors.get(session.memberId()),
                                        reactionsBySession.getOrDefault(session.id(), noReaction),
                                        commentCounts.getOrDefault(session.id(), 0L),
                                        readerId,
                                        now))
                .toList();
    }

    private Map<UUID, SessionView.ReactionsView> reactionsOf(List<UUID> sessionIds, UUID readerId) {
        Map<UUID, Map<ReactionKind, Long>> counts = new HashMap<>();
        for (ReactionCount count : reactions.countPerSessionAndKind(sessionIds)) {
            counts.computeIfAbsent(count.sessionId(), id -> new EnumMap<>(ReactionKind.class))
                    .put(count.kind(), count.count());
        }
        Map<UUID, ReactionKind> mine =
                reactions.findAllByMemberIdAndSessionIdIn(readerId, sessionIds).stream()
                        .collect(
                                Collectors.toMap(
                                        SessionReaction::sessionId, SessionReaction::kind));
        Map<UUID, SessionView.ReactionsView> views = new HashMap<>();
        for (var sessionId : sessionIds) {
            var found = counts.get(sessionId);
            if (found != null) {
                views.put(sessionId, SessionView.ReactionsView.of(found, mine.get(sessionId)));
            }
        }
        return views;
    }

    private static SessionView view(
            ClimbingSession session,
            Map<UUID, RouteSummaryView> summaries,
            MemberRefView author,
            SessionView.ReactionsView reactions,
            long commentCount,
            UUID readerId,
            Instant now) {
        // The foreign key guarantees that every route exists.
        var ascents =
                session.ascents().stream()
                        .map(
                                ascent ->
                                        new SessionView.AscentView(
                                                ascent.id(),
                                                summaries.get(ascent.routeId()),
                                                ascent.mode(),
                                                ascent.topped(),
                                                ascent.clean(),
                                                ascent.falls(),
                                                ascent.firstTry(),
                                                ascent.feeling()))
                        .toList();
        return new SessionView(
                session.id(),
                session.memberId(),
                author,
                session.climbedOn(),
                session.feeling(),
                session.comment(),
                session.photoId(),
                session.visibility(),
                session.createdAt(),
                session.memberId().equals(readerId)
                        && !SessionCalendar.isFrozen(session.climbedOn(), now),
                summary(ascents),
                ascents,
                reactions,
                commentCount);
    }

    private static SessionView.SummaryView summary(List<SessionView.AscentView> ascents) {
        var totals =
                SessionTotals.of(
                        ascents.stream()
                                .map(
                                        ascent ->
                                                new SessionTotals.Line(
                                                        ascent.mode(),
                                                        ascent.topped(),
                                                        ascent.route().grade(),
                                                        ascent.route().gradeRank()))
                                .toList());
        var hardest = totals.hardestTopped();
        return new SessionView.SummaryView(
                totals.ascentCount(),
                totals.toppedCount(),
                totals.leadCount(),
                hardest.map(SessionTotals.Line::grade).orElse(null),
                hardest.map(SessionTotals.Line::gradeRank).orElse(null));
    }
}
