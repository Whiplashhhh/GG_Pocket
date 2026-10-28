package fr.gravelinesgrimp.pocket.session.infra;

import fr.gravelinesgrimp.pocket.session.domain.SessionComment;
import fr.gravelinesgrimp.pocket.session.domain.SessionCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SessionCommentRepository extends JpaRepository<SessionComment, UUID> {

    /** Oldest first, deleted ones left out. */
    @Query(
            "select c from SessionComment c where c.sessionId = :sessionId and c.deletedAt is null"
                    + " order by c.createdAt, c.id")
    List<SessionComment> findVisibleBySessionId(UUID sessionId);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.SessionCount(c.sessionId, count(c))"
                    + " from SessionComment c where c.sessionId in :sessionIds"
                    + " and c.deletedAt is null group by c.sessionId")
    List<SessionCount> countVisiblePerSession(Collection<UUID> sessionIds);

    /** The latest visible comments (moderation), index {@code session_comment_latest_idx}. */
    @Query(
            value =
                    "select * from session_comment where deleted_at is null"
                            + " order by created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<SessionComment> findLatestVisible(int limit);

    /** The page after a cursor, in the same order as {@link #findLatestVisible}. */
    @Query(
            value =
                    "select * from session_comment where deleted_at is null"
                            + " and (created_at, id) < (:createdAt, :id)"
                            + " order by created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<SessionComment> findVisibleBefore(Instant createdAt, UUID id, int limit);

    /** My data export: deleted ones included, they are still kept. */
    List<SessionComment> findAllByAuthorIdOrderByCreatedAt(UUID authorId);

    /**
     * Account deletion: the comments on the sessions handed over to "Ancien·ne membre" go, nobody
     * sees those sessions any more.
     */
    @Modifying
    @Query(
            value =
                    "delete from session_comment where session_id in"
                            + " (select id from climbing_session where member_id = :memberId)",
            nativeQuery = true)
    void deleteAllOnSessionsOf(UUID memberId);
}
