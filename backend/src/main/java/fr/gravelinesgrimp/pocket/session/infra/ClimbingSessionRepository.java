package fr.gravelinesgrimp.pocket.session.infra;

import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClimbingSessionRepository extends JpaRepository<ClimbingSession, UUID> {

    Optional<ClimbingSession> findByMemberIdAndClientRef(UUID memberId, UUID clientRef);

    Optional<ClimbingSession> findByPhotoId(UUID photoId);

    @Query("select s from ClimbingSession s left join fetch s.ascents where s.id = :id")
    Optional<ClimbingSession> findWithAscentsById(UUID id);

    /** Callers never pass an empty collection. */
    @Query("select s from ClimbingSession s left join fetch s.ascents where s.id in :ids")
    List<ClimbingSession> findAllWithAscentsByIdIn(Collection<UUID> ids);

    /** My latest sessions (index {@code climbing_session_member_day_idx}). */
    @Query(
            value =
                    "select * from climbing_session where member_id = :memberId"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findLatest(UUID memberId, int limit);

    /** The page after a cursor, in the same order as {@link #findLatest}. */
    @Query(
            value =
                    "select * from climbing_session where member_id = :memberId"
                            + " and (climbed_on, created_at, id) < (:climbedOn, :createdAt, :id)"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findBefore(
            UUID memberId, LocalDate climbedOn, Instant createdAt, UUID id, int limit);
}
