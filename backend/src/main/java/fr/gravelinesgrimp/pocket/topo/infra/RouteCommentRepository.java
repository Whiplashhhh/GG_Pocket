package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.RouteComment;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RouteCommentRepository extends JpaRepository<RouteComment, UUID> {

    /** Oldest first, deleted ones left out. */
    @Query(
            "select c from RouteComment c where c.routeId = :routeId and c.deletedAt is null"
                    + " order by c.createdAt, c.id")
    List<RouteComment> findVisibleByRouteId(UUID routeId);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.topo.domain.RouteCount(c.routeId, count(c))"
                    + " from RouteComment c where c.routeId in :routeIds and c.deletedAt is null"
                    + " group by c.routeId")
    List<RouteCount> countVisiblePerRoute(Collection<UUID> routeIds);

    /** The latest visible comments (moderation), index {@code route_comment_latest_idx}. */
    @Query(
            value =
                    "select * from route_comment where deleted_at is null"
                            + " order by created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<RouteComment> findLatestVisible(int limit);

    /** The page after a cursor, in the same order as {@link #findLatestVisible}. */
    @Query(
            value =
                    "select * from route_comment where deleted_at is null"
                            + " and (created_at, id) < (:createdAt, :id)"
                            + " order by created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<RouteComment> findVisibleBefore(Instant createdAt, UUID id, int limit);

    /** My data export: deleted ones included, they are still kept. */
    List<RouteComment> findAllByAuthorIdOrderByCreatedAt(UUID authorId);
}
