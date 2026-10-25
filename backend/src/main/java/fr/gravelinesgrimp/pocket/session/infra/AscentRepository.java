package fr.gravelinesgrimp.pocket.session.infra;

import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.session.domain.ClimbedRoute;
import fr.gravelinesgrimp.pocket.session.domain.MyAscentLine;
import fr.gravelinesgrimp.pocket.session.domain.MyAscentRow;
import fr.gravelinesgrimp.pocket.session.domain.RouteAscentTotals;
import fr.gravelinesgrimp.pocket.session.domain.RouteMode;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AscentRepository extends JpaRepository<Ascent, UUID> {

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.topo.domain.RouteCount(a.routeId, count(a))"
                    + " from Ascent a where a.topped = true and a.routeId in :routeIds"
                    + " group by a.routeId")
    List<RouteCount> countToppedPerRoute(Collection<UUID> routeIds);

    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.RouteAscentTotals(count(a),"
                    + " count(case when a.topped = true then 1 end), count(distinct s.memberId),"
                    + " count(case when a.mode = :lead then 1 end))"
                    + " from Ascent a join a.session s where a.routeId = :routeId")
    RouteAscentTotals totalsOf(UUID routeId, ClimbMode lead);

    /** Newest first. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.MyAscentRow(s.id, s.climbedOn,"
                    + " a.mode, a.topped, a.clean, a.falls, a.firstTry)"
                    + " from Ascent a join a.session s"
                    + " where s.memberId = :memberId and a.routeId = :routeId"
                    + " order by s.climbedOn desc, s.createdAt desc, a.position desc")
    List<MyAscentRow> findMineOnRoute(UUID memberId, UUID routeId, Pageable page);

    /** Callers never pass an empty collection. May hold duplicates. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.RouteMode(a.routeId, a.mode)"
                    + " from Ascent a join a.session s"
                    + " where s.memberId = :memberId and a.clean = true and a.routeId in :routeIds")
    List<RouteMode> findCleanRouteModes(UUID memberId, Collection<UUID> routeIds);

    boolean existsBySession_MemberIdAndRouteIdAndModeAndCleanTrue(
            UUID memberId, UUID routeId, ClimbMode mode);

    /** Most recently climbed first. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.ClimbedRoute(a.routeId,"
                    + " max(s.climbedOn)) from Ascent a join a.session s"
                    + " where s.memberId = :memberId group by a.routeId"
                    + " order by max(s.climbedOn) desc")
    List<ClimbedRoute> findClimbedRoutes(UUID memberId);

    /** Every ascent of mine, in no particular order (the logbook). */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.MyAscentLine(s.id, s.climbedOn,"
                    + " a.routeId, a.mode, a.topped, a.clean)"
                    + " from Ascent a join a.session s where s.memberId = :memberId")
    List<MyAscentLine> findAllMine(UUID memberId);
}
