package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;
import fr.gravelinesgrimp.pocket.session.domain.SessionTotals;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.application.RouteViews;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds session views in a fixed number of queries whatever the number of sessions: the sessions
 * come with their ascents, and the routes of all of them are read and summarised in one go.
 */
@Component
public class SessionViews {

    private final RouteRepository routes;
    private final RouteViews routeViews;
    private final Clock clock;

    public SessionViews(RouteRepository routes, RouteViews routeViews, Clock clock) {
        this.routes = routes;
        this.routeViews = routeViews;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SessionView of(ClimbingSession session, UUID readerId) {
        return of(List.of(session), readerId).getFirst();
    }

    /** Views in the order of {@code sessions}. */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<SessionView> of(List<ClimbingSession> sessions, UUID readerId) {
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
        return sessions.stream().map(session -> view(session, summaries, readerId, now)).toList();
    }

    private static SessionView view(
            ClimbingSession session,
            Map<UUID, RouteSummaryView> summaries,
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
                session.climbedOn(),
                session.feeling(),
                session.comment(),
                session.photoId(),
                session.visibility(),
                session.createdAt(),
                session.memberId().equals(readerId)
                        && !SessionCalendar.isFrozen(session.climbedOn(), now),
                summary(ascents),
                ascents);
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
