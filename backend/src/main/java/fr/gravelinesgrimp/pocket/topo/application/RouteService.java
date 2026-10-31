package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.club.application.ClubLayoutReader;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteDraft;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.LastChanceAckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The routes of the topo (spec §8.4 and §8.10): every member reads them, setters and above open,
 * edit, announce the last chance, dismount and remount them (spec §5).
 */
@Service
public class RouteService {

    private static final Set<Role> SETTER_ROLES = Set.of(Role.SETTER, Role.BOARD, Role.ADMIN);

    private final RouteRepository routes;
    private final LastChanceAckRepository acknowledgements;
    private final MemberRepository members;
    private final ClubLayoutReader layouts;
    private final RouteViews views;
    private final AuditLog auditLog;
    private final Clock clock;

    public RouteService(
            RouteRepository routes,
            LastChanceAckRepository acknowledgements,
            MemberRepository members,
            ClubLayoutReader layouts,
            RouteViews views,
            AuditLog auditLog,
            Clock clock) {
        this.routes = routes;
        this.acknowledgements = acknowledgements;
        this.members = members;
        this.layouts = layouts;
        this.views = views;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    /** Routes on the wall (active and last chance), easiest first. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteSummaryView> listOnTheWall(UUID readerId) {
        var onTheWall =
                routes.findAllWithStylesByStatusIn(
                        List.of(RouteStatus.ACTIVE, RouteStatus.LAST_CHANCE));
        return views.summaries(onTheWall, readerId).stream()
                .sorted(RouteSummaryView.BY_GRADE)
                .toList();
    }

    /** The archive: dismounted routes, most recently dismounted first. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteSummaryView> listDismounted(UUID readerId) {
        var dismounted = routes.findAllWithStylesByStatusIn(List.of(RouteStatus.DISMOUNTED));
        return views.summaries(dismounted, readerId).stream()
                .sorted(RouteSummaryView.BY_DISMOUNT_DESCENDING)
                .toList();
    }

    /** Any route, dismounted ones included. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public RouteDetailView get(UUID readerId, UUID routeId) {
        return views.detail(load(routeId), readerId);
    }

    /** Who can be named as a setter: active members of role SETTER and above, by nickname. */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional(readOnly = true)
    public List<MemberRefView> listSetters() {
        return members.findAllByRolesAndStatus(SETTER_ROLES, MemberStatus.ACTIVE).stream()
                .map(MemberRefView::of)
                .toList();
    }

    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView create(UUID actorId, RouteDraft draft) {
        draft.check(ClubTime.today(clock));
        checkClubItems(draft, null);
        checkNewSetters(new HashSet<>(draft.setterIds()));
        var route = routes.save(new Route(draft, actorId));
        return views.detail(route, actorId);
    }

    /**
     * The cord and colours must be active only if they change: a route keeps a cord deactivated
     * after it was set. Likewise, only newly named setters are checked.
     */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView update(UUID actorId, UUID routeId, RouteDraft draft) {
        var route = load(routeId);
        route.ensureOnTheWall();
        draft.check(ClubTime.today(clock));
        checkClubItems(draft, route);
        var added = new HashSet<>(draft.setterIds());
        added.removeAll(route.setterIds());
        checkNewSetters(added);
        route.update(draft);
        return views.detail(route, actorId);
    }

    /** Announces, or re-dates, the last chance of a route (spec §8.9). */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView announceLastChance(UUID actorId, UUID routeId, LocalDate plannedOn) {
        var route = load(routeId);
        route.announceLastChance(plannedOn, ClubTime.today(clock));
        return views.detail(route, actorId);
    }

    /** Back to a plain active route: if it comes back to last chance, everyone is warned again. */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView cancelLastChance(UUID actorId, UUID routeId) {
        var route = lock(routeId);
        route.cancelLastChance();
        acknowledgements.deleteByRoute(routeId);
        return views.detail(route, actorId);
    }

    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView dismount(UUID actorId, UUID routeId) {
        var actor = loadMember(actorId);
        var route = lock(routeId);
        var before = route.status();
        route.dismount(clock.instant());
        acknowledgements.deleteByRoute(routeId);
        audit(actor, AuditAction.ROUTE_DISMOUNTED, route, before);
        return views.detail(route, actorId);
    }

    /** Undoes a dismount. Hearts on it count again (someone may be back at 3, D-075). */
    @PreAuthorize("hasRole('SETTER')")
    @Transactional
    public RouteDetailView remount(UUID actorId, UUID routeId) {
        var actor = loadMember(actorId);
        var route = load(routeId);
        var before = route.status();
        route.remount();
        audit(actor, AuditAction.ROUTE_REMOUNTED, route, before);
        return views.detail(route, actorId);
    }

    /**
     * Grade, cord and colours exist; the cord and colours are active unless the route already had
     * them.
     */
    private void checkClubItems(RouteDraft draft, Route existing) {
        var layout = layouts.read();
        layout.grade(draft.gradeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No grade"));
        var cord =
                layout.cord(draft.cordId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No cord"));
        if (!cord.active() && (existing == null || !existing.cordId().equals(cord.id()))) {
            throw new BusinessException(
                    ErrorCode.CLUB_ITEM_INACTIVE, "Cord " + cord.number() + " is inactive");
        }
        for (var colorId : draft.holdColorIds()) {
            var color =
                    layout.holdColor(colorId)
                            .orElseThrow(
                                    () -> new BusinessException(ErrorCode.NOT_FOUND, "No colour"));
            if (!color.active()
                    && (existing == null || !existing.holdColorIds().contains(colorId))) {
                throw new BusinessException(
                        ErrorCode.CLUB_ITEM_INACTIVE, "Colour " + color.label() + " is inactive");
            }
        }
    }

    /** Named setters are known, active members of role SETTER and above. */
    private void checkNewSetters(Set<UUID> setterIds) {
        if (setterIds.isEmpty()) {
            return;
        }
        var valid =
                members.findAllById(setterIds).stream()
                        .filter(member -> member.isActive() && member.role().isAtLeast(Role.SETTER))
                        .count();
        if (valid != setterIds.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_SETTERS, "Unknown, inactive or non-setter member named");
        }
    }

    private void audit(Member actor, AuditAction action, Route route, RouteStatus before) {
        auditLog.record(
                actor,
                action,
                AuditTargetType.ROUTE,
                route.id(),
                route.name(),
                before.name(),
                route.status().name());
    }

    private Route load(UUID routeId) {
        return routes.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No route"));
    }

    /** Like {@link #load}, holding the row until commit (the last chance pop-up relies on it). */
    private Route lock(UUID routeId) {
        return routes.lockById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No route"));
    }

    private Member loadMember(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
