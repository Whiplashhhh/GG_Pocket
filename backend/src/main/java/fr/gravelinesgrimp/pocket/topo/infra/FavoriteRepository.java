package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.Favorite;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Optional<Favorite> findByMemberIdAndRouteId(UUID memberId, UUID routeId);

    /** Newest first (spec §8.8: the history of my hearts). */
    List<Favorite> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);

    @Query("select f.routeId from Favorite f where f.memberId = :memberId")
    List<UUID> findRouteIdsByMemberId(UUID memberId);

    /** My hearts that count against the limit: routes not dismounted. */
    @Query(
            "select f.routeId from Favorite f, Route r"
                    + " where r.id = f.routeId and f.memberId = :memberId"
                    + " and r.status <> :dismounted")
    List<UUID> findRouteIdsStillOnTheWall(UUID memberId, RouteStatus dismounted);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.topo.domain.RouteCount(f.routeId, count(f))"
                    + " from Favorite f where f.routeId in :routeIds group by f.routeId")
    List<RouteCount> countPerRoute(Collection<UUID> routeIds);
}
