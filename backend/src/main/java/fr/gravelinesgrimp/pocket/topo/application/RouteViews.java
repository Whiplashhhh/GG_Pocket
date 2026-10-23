package fr.gravelinesgrimp.pocket.topo.application;

import fr.gravelinesgrimp.pocket.club.application.ClubLayoutReader;
import fr.gravelinesgrimp.pocket.club.domain.ClubLayout;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView.HoldView;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds route views for the services of the topo, in a fixed number of queries whatever the number
 * of routes: the club layout (4 small tables), the favourite and comment counts grouped by route,
 * and the marks of the reader. The routes come with their styles already fetched.
 */
@Component
public class RouteViews {

    private final ClubLayoutReader layouts;
    private final FavoriteRepository favorites;
    private final RouteCommentRepository comments;
    private final RouteCheckRepository checks;
    private final MemberRepository members;

    public RouteViews(
            ClubLayoutReader layouts,
            FavoriteRepository favorites,
            RouteCommentRepository comments,
            RouteCheckRepository checks,
            MemberRepository members) {
        this.layouts = layouts;
        this.favorites = favorites;
        this.comments = comments;
        this.checks = checks;
        this.members = members;
    }

    /** Summaries in the order of {@code routes}, with the marks of {@code readerId}. */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<RouteSummaryView> summaries(List<Route> routes, UUID readerId) {
        if (routes.isEmpty()) {
            return List.of();
        }
        var layout = layouts.read();
        var ids = routes.stream().map(Route::id).collect(Collectors.toSet());
        var favoriteCounts = byRoute(favorites.countPerRoute(ids));
        var commentCounts = byRoute(comments.countVisiblePerRoute(ids));
        var myFavorites = new HashSet<>(favorites.findRouteIdsByMemberId(readerId));
        Map<UUID, Set<ClimbMode>> myChecks =
                checks.findAllByMemberIdAndRouteIdIn(readerId, ids).stream()
                        .collect(
                                Collectors.groupingBy(
                                        RouteCheck::routeId,
                                        Collectors.mapping(RouteCheck::mode, Collectors.toSet())));
        return routes.stream()
                .map(
                        route ->
                                summary(
                                        route,
                                        layout,
                                        favoriteCounts.getOrDefault(route.id(), 0),
                                        commentCounts.getOrDefault(route.id(), 0),
                                        myFavorites.contains(route.id()),
                                        myChecks.getOrDefault(route.id(), Set.of())))
                .toList();
    }

    /** The route sheet, setters sorted by nickname. */
    @Transactional(propagation = Propagation.MANDATORY)
    public RouteDetailView detail(Route route, UUID readerId) {
        var summary = summaries(List.of(route), readerId).getFirst();
        var setters =
                members.findAllById(route.setterIds()).stream()
                        .sorted(
                                Comparator.comparing(
                                        (Member member) ->
                                                member.nickname().toLowerCase(Locale.ROOT)))
                        .map(MemberRefView::of)
                        .toList();
        return new RouteDetailView(
                summary,
                route.gradeId(),
                route.cordId(),
                setters,
                route.setterNote(),
                route.advice(),
                route.createdAt());
    }

    private static RouteSummaryView summary(
            Route route,
            ClubLayout layout,
            int favoriteCount,
            int commentCount,
            boolean favorite,
            Set<ClimbMode> myChecks) {
        // Foreign keys guarantee that grade, cord, sector and colours exist.
        var grade = layout.grades().get(route.gradeId());
        var cord = layout.cords().get(route.cordId());
        var sector = layout.sectors().get(cord.sectorId());
        var holds =
                route.holdColorIds().stream()
                        .map(layout.holdColors()::get)
                        .map(
                                color ->
                                        new HoldView(
                                                color.id(),
                                                color.label(),
                                                color.hex(),
                                                color.multicolor()))
                        .toList();
        return new RouteSummaryView(
                route.id(),
                route.name(),
                grade.code(),
                grade.rank(),
                cord.number(),
                sector.id(),
                sector.name(),
                sector.position(),
                holds,
                route.status(),
                route.openedOn(),
                route.plannedDismountOn(),
                route.dismountedAt(),
                route.photoId(),
                route.styles(),
                favoriteCount,
                commentCount,
                favorite,
                myChecks.stream().sorted().toList());
    }

    static Map<UUID, Integer> byRoute(List<RouteCount> counts) {
        return counts.stream()
                .collect(
                        Collectors.toMap(
                                RouteCount::routeId, routeCount -> routeCount.count().intValue()));
    }
}
