package fr.gravelinesgrimp.pocket.session.infra;

import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /** The latest sessions a climber shares with friends, in the order of {@link #findLatest}. */
    @Query(
            value =
                    "select * from climbing_session where member_id = :memberId"
                            + " and visibility = 'FRIENDS'"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findLatestShared(UUID memberId, int limit);

    /** The page after a cursor, in the same order as {@link #findLatestShared}. */
    @Query(
            value =
                    "select * from climbing_session where member_id = :memberId"
                            + " and visibility = 'FRIENDS'"
                            + " and (climbed_on, created_at, id) < (:climbedOn, :createdAt, :id)"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findSharedBefore(
            UUID memberId, LocalDate climbedOn, Instant createdAt, UUID id, int limit);

    /**
     * The feed (spec §8.2): my sessions and the ones my friends share with friends, in the order of
     * {@link #findLatest}. {@code friendIds} is never empty: callers add mine.
     */
    @Query(
            value =
                    "select * from climbing_session where (member_id = :memberId"
                            + " or (visibility = 'FRIENDS' and member_id in (:friendIds)))"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findLatestInFeed(UUID memberId, Collection<UUID> friendIds, int limit);

    /** The page after a cursor, in the same order as {@link #findLatestInFeed}. */
    @Query(
            value =
                    "select * from climbing_session where (member_id = :memberId"
                            + " or (visibility = 'FRIENDS' and member_id in (:friendIds)))"
                            + " and (climbed_on, created_at, id) < (:climbedOn, :createdAt, :id)"
                            + " order by climbed_on desc, created_at desc, id desc limit :limit",
            nativeQuery = true)
    List<ClimbingSession> findInFeedBefore(
            UUID memberId,
            Collection<UUID> friendIds,
            LocalDate climbedOn,
            Instant createdAt,
            UUID id,
            int limit);

    /** My sessions between two days (both included), with their ascents, oldest first. */
    @Query(
            "select s from ClimbingSession s left join fetch s.ascents"
                    + " where s.memberId = :memberId and s.climbedOn between :from and :to"
                    + " order by s.climbedOn, s.createdAt")
    List<ClimbingSession> findAllWithAscentsBetween(UUID memberId, LocalDate from, LocalDate to);

    /** Every day on which I logged a session. */
    @Query("select distinct s.climbedOn from ClimbingSession s where s.memberId = :memberId")
    List<LocalDate> findClimbingDays(UUID memberId);

    /** How many sessions each member logged (the members table, spec §8.11). */
    interface SessionCount {
        UUID getMemberId();

        long getSessions();
    }

    long countByMemberId(UUID memberId);

    @Query(
            "select s.memberId as memberId, count(s) as sessions from ClimbingSession s"
                    + " group by s.memberId")
    List<SessionCount> countByMember();

    /** Everything I logged, with the ascents, oldest first (my data export). */
    @Query(
            "select distinct s from ClimbingSession s left join fetch s.ascents"
                    + " where s.memberId = :memberId order by s.climbedOn, s.createdAt")
    List<ClimbingSession> findAllWithAscentsOf(UUID memberId);

    /**
     * Account deletion (spec §14): the sessions and their ascents go to "Ancien·ne membre" so the
     * routes' statistics stay true, without what was personal (comment, photo) and seen by nobody.
     */
    @Modifying
    @Query(
            value =
                    "update climbing_session set member_id = :heirId, comment = null,"
                            + " photo_id = null, visibility = 'PRIVATE', updated_at = :now,"
                            + " version = version + 1 where member_id = :memberId",
            nativeQuery = true)
    int handOver(UUID memberId, UUID heirId, Instant now);
}
