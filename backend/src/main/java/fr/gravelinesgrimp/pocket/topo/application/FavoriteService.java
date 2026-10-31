package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.CheckSource;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Favorite;
import fr.gravelinesgrimp.pocket.topo.domain.FavoriteLimit;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * What a member marks on routes: hearts ("coups de cœur", at most 2 on the wall, see {@link
 * FavoriteLimit}), "je sais la faire" in top rope or lead, and the favourite route of the profile.
 * Each member only ever touches their own marks.
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favorites;
    private final RouteCheckRepository checks;
    private final RouteRepository routes;
    private final MemberRepository members;
    private final RouteViews views;
    private final RouteAscentStats ascents;

    public FavoriteService(
            FavoriteRepository favorites,
            RouteCheckRepository checks,
            RouteRepository routes,
            MemberRepository members,
            RouteViews views,
            RouteAscentStats ascents) {
        this.favorites = favorites;
        this.checks = checks;
        this.routes = routes;
        this.members = members;
        this.views = views;
        this.ascents = ascents;
    }

    /**
     * Hearts a route, giving up {@code replaceRouteId} in the same go when the heart is full.
     * Idempotent. The member row is locked: two requests at once cannot go beyond 2.
     *
     * @throws BusinessException {@code ROUTE_DISMOUNTED}, {@code FAVORITES_FULL}
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void add(UUID memberId, UUID routeId, UUID replaceRouteId) {
        lockMember(memberId);
        loadRoute(routeId).ensureOnTheWall();
        var active = favorites.findRouteIdsStillOnTheWall(memberId, RouteStatus.DISMOUNTED);
        switch (FavoriteLimit.decide(active, routeId, replaceRouteId)) {
            case ALREADY_THERE -> {
                // Nothing to do.
            }
            case ADD -> favorites.save(new Favorite(memberId, routeId));
            case REPLACE -> {
                favorites
                        .findByMemberIdAndRouteId(memberId, replaceRouteId)
                        .ifPresent(replaced -> favorites.delete(replaced));
                favorites.save(new Favorite(memberId, routeId));
            }
        }
    }

    /** Idempotent. A heart on a dismounted route stays: that route is read-only. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void remove(UUID memberId, UUID routeId) {
        loadRoute(routeId).ensureOnTheWall();
        favorites
                .findByMemberIdAndRouteId(memberId, routeId)
                .ifPresent(favorite -> favorites.delete(favorite));
    }

    /** All my hearts, dismounted routes included, newest first. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteSummaryView> listMine(UUID memberId) {
        return listOf(memberId, memberId);
    }

    /**
     * The hearts of a member, as {@code readerId} sees them (their own heart on each route). Hearts
     * are public among members, like the rest of a profile.
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional(readOnly = true)
    public List<RouteSummaryView> listOf(UUID memberId, UUID readerId) {
        var mine = favorites.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        if (mine.isEmpty()) {
            return List.of();
        }
        Map<UUID, Route> byId =
                routes
                        .findAllWithStylesByIdIn(mine.stream().map(Favorite::routeId).toList())
                        .stream()
                        .collect(Collectors.toMap(Route::id, Function.identity()));
        var ordered = mine.stream().map(favorite -> byId.get(favorite.routeId())).toList();
        return views.summaries(ordered, readerId);
    }

    /** "Je sais la faire" in this mode. Idempotent. */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void check(UUID memberId, UUID routeId, ClimbMode mode) {
        lockMember(memberId);
        loadRoute(routeId).ensureOnTheWall();
        if (checks.findByMemberIdAndRouteIdAndMode(memberId, routeId, mode).isEmpty()) {
            checks.save(new RouteCheck(memberId, routeId, mode, CheckSource.MANUAL));
        }
    }

    /**
     * Idempotent. A mark backed by a clean ascent in this mode cannot be removed by hand: it would
     * come back with the next session (change or delete that session instead).
     *
     * @throws BusinessException {@code ROUTE_DISMOUNTED}, {@code CHECK_FROM_ASCENT}
     */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void uncheck(UUID memberId, UUID routeId, ClimbMode mode) {
        lockMember(memberId);
        loadRoute(routeId).ensureOnTheWall();
        if (ascents.hasCleanAscent(memberId, routeId, mode)) {
            throw new BusinessException(
                    ErrorCode.CHECK_FROM_ASCENT, "Clean ascent of " + routeId + " in " + mode);
        }
        checks.findByMemberIdAndRouteIdAndMode(memberId, routeId, mode)
                .ifPresent(check -> checks.delete(check));
    }

    /** The favourite route of my profile; a dismounted route is fine (spec §7). */
    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void chooseFavoriteRoute(UUID memberId, UUID routeId) {
        var member = loadMember(memberId);
        loadRoute(routeId);
        member.changeFavoriteRoute(routeId);
    }

    @PreAuthorize("hasRole('TOURIST')")
    @Transactional
    public void clearFavoriteRoute(UUID memberId) {
        loadMember(memberId).changeFavoriteRoute(null);
    }

    /** Locks the member row until the end of the transaction (see MemberRepository#lockById). */
    private Member lockMember(UUID memberId) {
        return members.lockById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    private Member loadMember(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    private Route loadRoute(UUID routeId) {
        return routes.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No route"));
    }
}
