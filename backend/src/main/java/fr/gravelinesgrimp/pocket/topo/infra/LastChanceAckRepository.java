package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.LastChanceAck;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LastChanceAckRepository extends JpaRepository<LastChanceAck, UUID> {

    /** Callers never pass an empty collection. */
    @Query(
            "select a.routeId from LastChanceAck a"
                    + " where a.memberId = :memberId and a.routeId in :routeIds")
    List<UUID> findRouteIdsAcknowledgedBy(UUID memberId, Collection<UUID> routeIds);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.topo.domain.RouteCount(a.routeId, count(a))"
                    + " from LastChanceAck a where a.routeId in :routeIds group by a.routeId")
    List<RouteCount> countPerRoute(Collection<UUID> routeIds);

    /** The route left last chance: if it comes back, everyone is warned again. */
    @Modifying
    @Query("delete from LastChanceAck a where a.routeId = :routeId")
    void deleteByRoute(UUID routeId);

    /** My data export. */
    List<LastChanceAck> findAllByMemberIdOrderByCreatedAt(UUID memberId);
}
