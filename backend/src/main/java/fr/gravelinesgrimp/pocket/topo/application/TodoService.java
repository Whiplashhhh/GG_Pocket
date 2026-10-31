package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.domain.TodoStates;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * "À faire" (spec §8.5): the routes on the wall I cannot climb yet, per mode. Dismounted routes
 * never show up. Ticking and unticking go through {@link FavoriteService}.
 */
@Service
public class TodoService {

    private final RouteRepository routes;
    private final RouteCheckRepository checks;
    private final RouteViews views;
    private final RouteAscentStats ascents;

    public TodoService(
            RouteRepository routes,
            RouteCheckRepository checks,
            RouteViews views,
            RouteAscentStats ascents) {
        this.routes = routes;
        this.checks = checks;
        this.views = views;
        this.ascents = ascents;
    }

    /** Routes easiest first, like the topo. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public TodoView of(UUID memberId) {
        var onTheWall =
                views
                        .summaries(
                                routes.findAllWithStylesByStatusIn(
                                        List.of(RouteStatus.ACTIVE, RouteStatus.LAST_CHANCE)),
                                memberId)
                        .stream()
                        .sorted(RouteSummaryView.BY_GRADE)
                        .toList();
        if (onTheWall.isEmpty()) {
            return new TodoView(0, 0, 0, List.of());
        }
        var ids = onTheWall.stream().map(RouteSummaryView::id).collect(Collectors.toSet());
        Map<UUID, Set<ClimbMode>> marks =
                checks.findAllByMemberIdAndRouteIdIn(memberId, ids).stream()
                        .collect(
                                Collectors.groupingBy(
                                        RouteCheck::routeId,
                                        Collectors.mapping(RouteCheck::mode, Collectors.toSet())));
        var clean = ascents.cleanModesOf(memberId, ids);
        var todos =
                onTheWall.stream()
                        .map(
                                route -> {
                                    var states =
                                            TodoStates.of(
                                                    marks.getOrDefault(route.id(), Set.of()),
                                                    clean.getOrDefault(route.id(), Set.of()));
                                    return new TodoView.RouteTodo(
                                            route, states.topRope(), states.lead());
                                })
                        .toList();
        return new TodoView(
                todos.size(),
                (int) todos.stream().filter(todo -> todo.topRope().known()).count(),
                (int) todos.stream().filter(todo -> todo.lead().known()).count(),
                todos);
    }
}
