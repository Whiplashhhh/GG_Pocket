package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * What the topo needs to know about the ascents logged in sessions. Implemented by the {@code
 * session} module, so that the topo does not depend on it. Called inside the caller's transaction.
 */
public interface RouteAscentStats {

    /** Most of my own ascents shown on a route sheet. */
    int MY_ASCENTS_LIMIT = 20;

    /** Topped ascents per route, by everyone; routes without any are left out. */
    Map<UUID, Integer> toppedCountPerRoute(Collection<UUID> routeIds);

    RouteStatsView statsOf(UUID routeId);

    /** My ascents of a route, newest first, at most {@link #MY_ASCENTS_LIMIT}. */
    List<MyRouteAscentView> ascentsOf(UUID memberId, UUID routeId);

    /** Whether I have a clean ascent of the route in this mode ("je sais la faire" automatic). */
    boolean hasCleanAscent(UUID memberId, UUID routeId, ClimbMode mode);

    /** The modes in which I climbed each route clean; routes without any are left out. */
    Map<UUID, Set<ClimbMode>> cleanModesOf(UUID memberId, Collection<UUID> routeIds);
}
