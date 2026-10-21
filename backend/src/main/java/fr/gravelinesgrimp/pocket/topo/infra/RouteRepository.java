package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lists fetch the styles in the same query: one query for the whole topo. */
public interface RouteRepository extends JpaRepository<Route, UUID> {

    @Query("select r from Route r left join fetch r.styles where r.status in :statuses")
    List<Route> findAllWithStylesByStatusIn(Collection<RouteStatus> statuses);

    /** Callers never pass an empty collection. */
    @Query("select r from Route r left join fetch r.styles where r.id in :ids")
    List<Route> findAllWithStylesByIdIn(Collection<UUID> ids);

    @Query("select r from Route r left join fetch r.styles where r.id = :id")
    Optional<Route> findWithStylesById(UUID id);

    /** Routes with this status that the member has not acknowledged (the last chance pop-up). */
    @Query(
            "select r from Route r left join fetch r.styles where r.status = :status"
                    + " and not exists (select a.id from LastChanceAck a"
                    + " where a.routeId = r.id and a.memberId = :memberId)")
    List<Route> findAllWithStylesByStatusNotAcknowledgedBy(RouteStatus status, UUID memberId);

    /** Callers never pass an empty collection. */
    @Query("select r.id from Route r where r.status = :status and r.id in :ids")
    List<UUID> findIdsByStatusAndIdIn(RouteStatus status, Collection<UUID> ids);
}
