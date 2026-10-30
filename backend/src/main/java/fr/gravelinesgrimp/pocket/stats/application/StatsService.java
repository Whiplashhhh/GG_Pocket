package fr.gravelinesgrimp.pocket.stats.application;

import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.PlanningMonth;
import fr.gravelinesgrimp.pocket.stats.domain.Attendance;
import fr.gravelinesgrimp.pocket.stats.infra.AttendanceRepository;
import fr.gravelinesgrimp.pocket.stats.infra.RouteAttendanceRow;
import fr.gravelinesgrimp.pocket.topo.application.RouteViews;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The statistics of the management space (spec §8.10), for setters and above: the attendance of a
 * month, route by route, and the routes on the wall today with their ascents since they opened.
 */
@Service
public class StatsService {

    private final AttendanceRepository attendance;
    private final RouteRepository routes;
    private final RouteViews views;
    private final Clock clock;

    StatsService(
            AttendanceRepository attendance,
            RouteRepository routes,
            RouteViews views,
            Clock clock) {
        this.attendance = attendance;
        this.routes = routes;
        this.views = views;
        this.clock = clock;
    }

    /**
     * Most climbed first. A route dismounted before the month but climbed in it (a session logged
     * late) is listed too.
     *
     * @param month {@code YYYY-MM}, or null for this month
     * @throws fr.gravelinesgrimp.pocket.platform.domain.BusinessException {@code VALIDATION_FAILED}
     *     for a malformed or future month
     */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional(readOnly = true)
    public MonthAttendanceView ofMonth(UUID readerId, String month) {
        var shown = PlanningMonth.parse(month, ClubTime.today(clock));
        var from = shown.atDay(1);
        var to = shown.atEndOfMonth();
        var counts = byRoute(attendance.perRouteBetween(from, to));
        var listed =
                new ArrayList<>(
                        routes.findAllWithStylesOnTheWallDuring(
                                from.atStartOfDay(ClubTime.ZONE).toInstant(), to));
        var missing = new HashSet<>(counts.keySet());
        listed.forEach(route -> missing.remove(route.id()));
        if (!missing.isEmpty()) {
            listed.addAll(routes.findAllWithStylesByIdIn(missing));
        }
        return new MonthAttendanceView(
                shown, attendance.totalBetween(from, to), lines(listed, counts, readerId));
    }

    /** The routes on the wall (last chance included), most climbed since they opened first. */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional(readOnly = true)
    public List<RouteAttendanceView> onTheWall(UUID readerId) {
        var onTheWall =
                routes.findAllWithStylesByStatusIn(
                        List.of(RouteStatus.ACTIVE, RouteStatus.LAST_CHANCE));
        if (onTheWall.isEmpty()) {
            return List.of();
        }
        var counts = byRoute(attendance.perRoute(onTheWall.stream().map(Route::id).toList()));
        return lines(onTheWall, counts, readerId);
    }

    private List<RouteAttendanceView> lines(
            List<Route> listed, Map<UUID, Attendance> counts, UUID readerId) {
        return views.summaries(listed, readerId).stream()
                .map(
                        route ->
                                new RouteAttendanceView(
                                        route, counts.getOrDefault(route.id(), Attendance.NONE)))
                .sorted(
                        Comparator.comparingLong(
                                        (RouteAttendanceView line) ->
                                                line.attendance().ascentCount())
                                .reversed()
                                .thenComparingInt(line -> line.route().cordNumber())
                                .thenComparingInt(line -> line.route().gradeRank()))
                .toList();
    }

    private static Map<UUID, Attendance> byRoute(List<RouteAttendanceRow> rows) {
        return rows.stream()
                .collect(
                        Collectors.toMap(
                                RouteAttendanceRow::routeId, RouteAttendanceRow::toAttendance));
    }
}
