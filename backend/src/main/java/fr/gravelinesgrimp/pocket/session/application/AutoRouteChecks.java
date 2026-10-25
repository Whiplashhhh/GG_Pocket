package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.session.domain.AutoChecks;
import fr.gravelinesgrimp.pocket.session.domain.RouteMode;
import fr.gravelinesgrimp.pocket.session.infra.AscentRepository;
import fr.gravelinesgrimp.pocket.topo.domain.CheckSource;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Writes the "je sais la faire" marks deduced from ascents (rules in {@link AutoChecks}). */
@Component
public class AutoRouteChecks {

    private final AscentRepository ascents;
    private final RouteCheckRepository checks;

    public AutoRouteChecks(AscentRepository ascents, RouteCheckRepository checks) {
        this.ascents = ascents;
        this.checks = checks;
    }

    /**
     * Brings the marks of the touched pairs in line with the ascents, once the change of sessions
     * is applied (the queries below flush it first).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void refresh(UUID memberId, Set<RouteMode> touched) {
        if (touched.isEmpty()) {
            return;
        }
        var routeIds = touched.stream().map(RouteMode::routeId).collect(Collectors.toSet());
        var clean = new HashSet<>(ascents.findCleanRouteModes(memberId, routeIds));
        Map<RouteMode, RouteCheck> existing =
                checks.findAllByMemberIdAndRouteIdIn(memberId, routeIds).stream()
                        .collect(
                                Collectors.toMap(
                                        check -> new RouteMode(check.routeId(), check.mode()),
                                        Function.identity()));
        var sources =
                existing.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey, entry -> entry.getValue().source()));
        var plan = AutoChecks.plan(touched, clean, sources);
        plan.toDelete().forEach(pair -> checks.delete(existing.get(pair)));
        plan.toCreate()
                .forEach(
                        pair ->
                                checks.save(
                                        new RouteCheck(
                                                memberId,
                                                pair.routeId(),
                                                pair.mode(),
                                                CheckSource.ASCENT)));
    }
}
