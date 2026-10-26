package fr.gravelinesgrimp.pocket.session.application;

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

/** My logbook ("carnet"): everything I climbed, in a fixed number of queries. */
@Service
public class LogbookService {

    private final AscentRepository ascents;
    private final RouteRepository routes;
    private final RouteViews routeViews;

    public LogbookService(AscentRepository ascents, RouteRepository routes, RouteViews routeViews) {
        this.ascents = ascents;
        this.routes = routes;
        this.routeViews = routeViews;
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public LogbookView of(UUID memberId) {
        var mine = ascents.findAllMine(memberId);
        var routeIds = mine.stream().map(MyAscentLine::routeId).collect(Collectors.toSet());
        Map<UUID, RouteSummaryView> summaries =
                routeIds.isEmpty()
                        ? Map.of()
                        : routeViews
                                .summaries(routes.findAllWithStylesByIdIn(routeIds), memberId)
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
