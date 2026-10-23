package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.CheckSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Je sais la faire" deduced from ascents (spec §8.5): a route is known in a mode as soon as the
 * climber has a clean ascent of it in that mode. Only the marks of source {@code ASCENT} are added
 * or removed here; a mark ticked by hand ({@code MANUAL}) is never touched.
 */
public final class AutoChecks {

    /** What to write for the pairs that were touched. */
    public record Plan(Set<RouteMode> toCreate, Set<RouteMode> toDelete) {}

    private AutoChecks() {}

    /**
     * @param touched the pairs whose ascents changed (before and after the change)
     * @param clean among them, those that still have a clean ascent
     * @param existing the current marks on the touched pairs, with their source
     */
    public static Plan plan(
            Set<RouteMode> touched, Set<RouteMode> clean, Map<RouteMode, CheckSource> existing) {
        var toCreate =
                touched.stream()
                        .filter(pair -> clean.contains(pair) && !existing.containsKey(pair))
                        .collect(Collectors.toSet());
        var toDelete =
                touched.stream()
                        .filter(
                                pair ->
                                        !clean.contains(pair)
                                                && existing.get(pair) == CheckSource.ASCENT)
                        .collect(Collectors.toSet());
        return new Plan(toCreate, toDelete);
    }
}
