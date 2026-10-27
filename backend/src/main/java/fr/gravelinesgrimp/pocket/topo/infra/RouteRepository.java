package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Shared lock on the routes still in this status: a concurrent status change (which takes
     * {@link #lockById}) waits for the caller, or makes it see the new status. Callers never pass
     * an empty collection.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select r from Route r where r.status = :status and r.id in :ids")
    List<Route> lockAllByStatusAndIdIn(RouteStatus status, Collection<UUID> ids);

    /** Exclusive lock before a status change that also touches the acknowledgements. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Route r where r.id = :id")
    Optional<Route> lockById(UUID id);

    /**
     * Account deletion (spec §14): "Ancien·ne membre" is credited on the routes the member set,
     * before the member's own rows go with the account ({@code on delete cascade}).
     */
    @Modifying
    @Query(
            value =
                    "insert into route_setter (route_id, member_id)"
                            + " select route_id, :heirId from route_setter where member_id = :memberId"
                            + " on conflict do nothing",
            nativeQuery = true)
    int creditSetter(UUID memberId, UUID heirId);

    /** The routes I set (my data export). */
    @Query(
            "select r from Route r join r.setterIds setter where setter = :memberId order by r.openedOn")
    List<Route> findAllSetBy(UUID memberId);
}
