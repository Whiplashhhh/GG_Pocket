package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.RouteMode;
import fr.gravelinesgrimp.pocket.session.infra.AscentRepository;
import fr.gravelinesgrimp.pocket.topo.application.MyRouteAscentView;
import fr.gravelinesgrimp.pocket.topo.application.RouteAscentStats;
import fr.gravelinesgrimp.pocket.topo.application.RouteStatsView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** The ascents of the sessions, as the topo sees them. */
@Component
class SessionRouteAscentStats implements RouteAscentStats {

    private final AscentRepository ascents;

    SessionRouteAscentStats(AscentRepository ascents) {
        this.ascents = ascents;
    }

    @Override
    public Map<UUID, Integer> toppedCountPerRoute(Collection<UUID> routeIds) {
        if (routeIds.isEmpty()) {
            return Map.of();
        }
        return ascents.countToppedPerRoute(routeIds).stream()
                .collect(
                        Collectors.toMap(
                                RouteCount::routeId, routeCount -> routeCount.count().intValue()));
    }

    @Override
    public RouteStatsView statsOf(UUID routeId) {
        var totals = ascents.totalsOf(routeId, ClimbMode.LEAD);
        return RouteStatsView.of(
                totals.ascentCount(),
                totals.toppedCount(),
                totals.climberCount(),
                totals.leadCount());
    }

    @Override
    public List<MyRouteAscentView> ascentsOf(UUID memberId, UUID routeId) {
        return ascents
                .findMineOnRoute(memberId, routeId, PageRequest.of(0, MY_ASCENTS_LIMIT))
                .stream()
                .map(
                        row ->
                                new MyRouteAscentView(
                                        row.sessionId(),
                                        row.climbedOn(),
                                        row.mode(),
                                        row.topped(),
                                        row.clean(),
                                        row.falls(),
                                        row.firstTry()))
                .toList();
    }

    @Override
    public boolean hasCleanAscent(UUID memberId, UUID routeId, ClimbMode mode) {
        return ascents.existsBySession_MemberIdAndRouteIdAndModeAndCleanTrue(
                memberId, routeId, mode);
    }

    @Override
    public Map<UUID, Set<ClimbMode>> cleanModesOf(UUID memberId, Collection<UUID> routeIds) {
        if (routeIds.isEmpty()) {
            return Map.of();
        }
        return ascents.findCleanRouteModes(memberId, routeIds).stream()
                .collect(
                        Collectors.groupingBy(
                                RouteMode::routeId,
                                Collectors.mapping(RouteMode::mode, Collectors.toSet())));
    }
}
