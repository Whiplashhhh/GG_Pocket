package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.session.domain.Logbook;
import fr.gravelinesgrimp.pocket.session.domain.MyAscentLine;
import fr.gravelinesgrimp.pocket.session.infra.AscentRepository;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;
import fr.gravelinesgrimp.pocket.topo.application.RouteViews;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The logbook ("carnet"): everything a climber climbed, in a fixed number of queries. Mine holds
 * every session; a friend's holds the sessions they share with friends.
 */
@Service
public class LogbookService {

    private final AscentRepository ascents;
    private final RouteRepository routes;
    private final RouteViews routeViews;
    private final SessionAudience audience;

    public LogbookService(
            AscentRepository ascents,
            RouteRepository routes,
            RouteViews routeViews,
            SessionAudience audience) {
        this.ascents = ascents;
        this.routes = routes;
        this.routeViews = routeViews;
        this.audience = audience;
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public LogbookView of(UUID memberId) {
        return build(ascents.findAllMine(memberId), memberId);
    }

    /**
     * @throws BusinessException {@code NOT_FOUND} (not a friend, or not an active member)
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public LogbookView ofFriend(UUID readerId, UUID climberId) {
        if (readerId.equals(climberId)) {
            return of(readerId);
        }
        audience.checkFriendOf(readerId, climberId);
        return build(ascents.findAllShared(climberId), readerId);
    }

    private LogbookView build(List<MyAscentLine> mine, UUID readerId) {
        var routeIds = mine.stream().map(MyAscentLine::routeId).collect(Collectors.toSet());
        Map<UUID, RouteSummaryView> summaries =
                routeIds.isEmpty()
                        ? Map.of()
                        : routeViews
                                .summaries(routes.findAllWithStylesByIdIn(routeIds), readerId)
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                RouteSummaryView::id, Function.identity()));
        // The foreign key guarantees that every route exists.
        var logbook =
                Logbook.of(
                        mine,
                        routeId -> {
                            var route = summaries.get(routeId);
                            return new Logbook.Grade(route.grade(), route.gradeRank());
                        });
        List<LogbookView.RouteView> lines =
                logbook.routes().stream()
                        .map(line -> new LogbookView.RouteView(summaries.get(line.routeId()), line))
                        .toList();
        return new LogbookView(logbook.totals(), logbook.grades(), lines);
    }
}
