package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.LastChanceAck;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.LastChanceAckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * The last chance pop-up (spec §8.9): every member is warned once about each route announced in
 * last chance, and "J'accepte leurs destins" records it. Setters see how many members saw it.
 */
@Service
public class LastChanceService {

    /** A route in last chance and how many members acknowledged it. */
    public record LastChanceRouteView(RouteSummaryView route, int acknowledgedCount) {}

    private final RouteRepository routes;
    private final LastChanceAckRepository acknowledgements;
    private final MemberRepository members;
    private final RouteViews views;

    public LastChanceService(
            RouteRepository routes,
            LastChanceAckRepository acknowledgements,
            MemberRepository members,
            RouteViews views) {
        this.routes = routes;
        this.acknowledgements = acknowledgements;
        this.members = members;
        this.views = views;
    }

    /** Soonest planned dismount first. */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional(readOnly = true)
    public List<LastChanceRouteView> listWithAcknowledgements(UUID readerId) {
        var inLastChance = routes.findAllWithStylesByStatusIn(List.of(RouteStatus.LAST_CHANCE));
        if (inLastChance.isEmpty()) {
            return List.of();
        }
        var counts =
                RouteViews.byRoute(
                        acknowledgements.countPerRoute(
                                inLastChance.stream().map(Route::id).toList()));
        return views.summaries(inLastChance, readerId).stream()
                .sorted(RouteSummaryView.BY_PLANNED_DISMOUNT)
                .map(route -> new LastChanceRouteView(route, counts.getOrDefault(route.id(), 0)))
                .toList();
    }

    /** What the pop-up lists: routes in last chance I have not acknowledged yet. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteSummaryView> listUnacknowledged(UUID memberId) {
        var pending =
                routes.findAllWithStylesByStatusNotAcknowledgedBy(
                        RouteStatus.LAST_CHANCE, memberId);
        return views.summaries(pending, memberId).stream()
                .sorted(RouteSummaryView.BY_PLANNED_DISMOUNT)
                .toList();
    }

    /**
     * Records that I saw these routes. Ids of routes no longer in last chance are ignored (the
     * pop-up may be a little late); already acknowledged ones too.
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void acknowledge(UUID memberId, List<UUID> routeIds) {
        // One request at a time per member: no duplicate acknowledgement.
        members.lockById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
        if (routeIds.isEmpty()) {
            return;
        }
        var inLastChance =
                routes
                        .lockAllByStatusAndIdIn(RouteStatus.LAST_CHANCE, new HashSet<>(routeIds))
                        .stream()
                        .map(Route::id)
                        .toList();
        if (inLastChance.isEmpty()) {
            return;
        }
        var already =
                new HashSet<>(acknowledgements.findRouteIdsAcknowledgedBy(memberId, inLastChance));
        inLastChance.stream()
                .filter(routeId -> !already.contains(routeId))
                .forEach(routeId -> acknowledgements.save(new LastChanceAck(memberId, routeId)));
    }
}
