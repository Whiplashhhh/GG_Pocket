package fr.gravelinesgrimp.pocket.session.infra;

import fr.gravelinesgrimp.pocket.session.domain.ReactionCount;
import fr.gravelinesgrimp.pocket.session.domain.SessionReaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SessionReactionRepository extends JpaRepository<SessionReaction, UUID> {

    /**
     * Gives or changes my reaction in one statement: two taps at once leave one row, the last kind.
     * {@code kind} is a {@code ReactionKind} name.
     */
    @Modifying
    @Query(
            value =
                    "insert into session_reaction"
                            + " (id, session_id, member_id, kind, created_at, updated_at, version)"
                            + " values (:id, :sessionId, :memberId, :kind, :now, :now, 0)"
                            + " on conflict (session_id, member_id) do update"
                            + " set kind = excluded.kind, updated_at = excluded.updated_at,"
                            + " version = session_reaction.version + 1"
                            + " where session_reaction.kind <> excluded.kind",
            nativeQuery = true)
    void upsert(UUID id, UUID sessionId, UUID memberId, String kind, Instant now);

    @Modifying
    @Query(
            "delete from SessionReaction r where r.sessionId = :sessionId and r.memberId = :memberId")
    void deleteBySessionIdAndMemberId(UUID sessionId, UUID memberId);

    /** Callers never pass an empty collection. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.session.domain.ReactionCount("
                    + "r.sessionId, r.kind, count(r))"
                    + " from SessionReaction r where r.sessionId in :sessionIds"
                    + " group by r.sessionId, r.kind")
    List<ReactionCount> countPerSessionAndKind(Collection<UUID> sessionIds);

    /** Mine among these sessions. Callers never pass an empty collection. */
    List<SessionReaction> findAllByMemberIdAndSessionIdIn(
            UUID memberId, Collection<UUID> sessionIds);

    /** My data export. */
    List<SessionReaction> findAllByMemberIdOrderByCreatedAt(UUID memberId);

    /**
     * Account deletion: the reactions to the sessions handed over to "Ancien·ne membre" go, nobody
     * sees those sessions any more.
     */
    @Modifying
    @Query(
            value =
                    "delete from session_reaction where session_id in"
                            + " (select id from climbing_session where member_id = :memberId)",
            nativeQuery = true)
    void deleteAllOnSessionsOf(UUID memberId);
}
