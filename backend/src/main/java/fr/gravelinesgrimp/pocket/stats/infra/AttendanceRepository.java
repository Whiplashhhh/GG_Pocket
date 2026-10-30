package fr.gravelinesgrimp.pocket.stats.infra;

import fr.gravelinesgrimp.pocket.session.domain.Ascent;
import fr.gravelinesgrimp.pocket.stats.domain.Attendance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The ascents logged in sessions, counted for the management space (spec §8.10). Every session
 * counts, whatever its visibility: only totals come out, never who climbed.
 */
public interface AttendanceRepository extends Repository<Ascent, UUID> {

    /** Per route climbed between two days; routes without any ascent are left out. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.stats.infra.RouteAttendanceRow(a.routeId,"
                    + " count(a), count(case when a.topped = true then 1 end),"
                    + " count(case when a.clean = true then 1 end), count(distinct s.memberId))"
                    + " from ClimbingSession s join s.ascents a"
                    + " where s.climbedOn between :from and :to group by a.routeId")
    List<RouteAttendanceRow> perRouteBetween(LocalDate from, LocalDate to);

    /** Every route together between two days: a climber of several routes counts once. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.stats.domain.Attendance(count(a),"
                    + " count(case when a.topped = true then 1 end),"
                    + " count(case when a.clean = true then 1 end), count(distinct s.memberId))"
                    + " from ClimbingSession s join s.ascents a"
                    + " where s.climbedOn between :from and :to")
    Attendance totalBetween(LocalDate from, LocalDate to);

    /** Since ever, per route; routes without any ascent are left out. Never an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.stats.infra.RouteAttendanceRow(a.routeId,"
                    + " count(a), count(case when a.topped = true then 1 end),"
                    + " count(case when a.clean = true then 1 end), count(distinct s.memberId))"
                    + " from ClimbingSession s join s.ascents a"
                    + " where a.routeId in :routeIds group by a.routeId")
    List<RouteAttendanceRow> perRoute(Collection<UUID> routeIds);
}
