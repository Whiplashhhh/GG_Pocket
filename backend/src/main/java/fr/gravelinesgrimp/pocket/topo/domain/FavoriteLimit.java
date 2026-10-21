package fr.gravelinesgrimp.pocket.topo.domain;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import java.util.Collection;
import java.util.UUID;

/**
 * At most 2 favourites ("coups de cœur") on routes still on the wall (DECISIONS D-010). Hearts on
 * dismounted routes free their place but stay in the history. Remounting a route may bring someone
 * to 3: tolerated, they just cannot add another one without giving one up (D-075).
 */
public final class FavoriteLimit {

    public static final int MAX_ACTIVE = 2;

    /** What hearting a route does. */
    public enum Decision {
        /** Already one of my favourites: nothing to do. */
        ALREADY_THERE,
        ADD,
        /** Add it and give up {@code replaceRouteId}, in one go. */
        REPLACE
    }

    private FavoriteLimit() {}

    /**
     * @param activeFavorites my favourite routes that are still on the wall
     * @param routeId the route to heart (on the wall)
     * @param replaceRouteId one of my active favourites to give up, or null
     * @throws BusinessException {@code FAVORITES_FULL} if my heart is full and nothing valid is
     *     given up
     */
    public static Decision decide(
            Collection<UUID> activeFavorites, UUID routeId, UUID replaceRouteId) {
        if (activeFavorites.contains(routeId)) {
            return Decision.ALREADY_THERE;
        }
        if (replaceRouteId != null && activeFavorites.contains(replaceRouteId)) {
            return Decision.REPLACE;
        }
        if (activeFavorites.size() < MAX_ACTIVE) {
            return Decision.ADD;
        }
        throw new BusinessException(
                ErrorCode.FAVORITES_FULL, activeFavorites.size() + " active favourites already");
    }
}
