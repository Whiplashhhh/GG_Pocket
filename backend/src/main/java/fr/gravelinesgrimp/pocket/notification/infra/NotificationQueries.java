package fr.gravelinesgrimp.pocket.notification.infra;

import fr.gravelinesgrimp.pocket.notification.domain.NotificationKind;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The bell, read from the tables that already say what happened (D-128): nothing to keep in step,
 * and a deleted comment, a reaction taken back or a request withdrawn leaves the bell by itself.
 */
@Repository
public class NotificationQueries {

    /** Length of a comment's excerpt in the bell. */
    static final int EXCERPT_LENGTH = 140;

    private static final String EVENTS =
            """
            select * from (
              select 'REACTION' as kind, r.id as source_id, r.updated_at as at,
                     r.member_id as actor_id, s.id as session_id, s.member_id as session_owner_id,
                     s.climbed_on, r.kind as reaction, null::text as excerpt
                from session_reaction r
                join climbing_session s on s.id = r.session_id
               where s.member_id = :me and r.member_id <> :me and r.updated_at > :since
              union all
              select case when p.author_id = :me then 'REPLY' else 'COMMENT' end, c.id,
                     c.created_at, c.author_id, s.id, s.member_id, s.climbed_on, null::varchar,
                     left(c.body, :excerpt)
                from session_comment c
                join climbing_session s on s.id = c.session_id
                left join session_comment p on p.id = c.reply_to_id
               where c.deleted_at is null and c.author_id <> :me and c.created_at > :since
                 and (s.member_id = :me or p.author_id = :me)
              union all
              select 'FRIEND_SESSION', s.id, s.created_at, s.member_id, s.id, s.member_id,
                     s.climbed_on, null::varchar, null::text
                from climbing_session s
               where s.member_id in (:friends) and s.member_id <> :me
                 and s.visibility in ('FRIENDS', 'CLUB') and s.created_at > :since
              union all
              select 'FRIEND_REQUEST', f.id, f.created_at, f.requester_id, null::uuid, null::uuid,
                     null::date, null::varchar, null::text
                from friendship f
               where f.addressee_id = :me and f.status = 'PENDING' and f.created_at > :since
              union all
              select 'FRIEND_ACCEPTED', f.id, f.accepted_at, f.addressee_id, null::uuid, null::uuid,
                     null::date, null::varchar, null::text
                from friendship f
               where f.requester_id = :me and f.status = 'ACCEPTED' and f.accepted_at > :since
            ) events
            order by at desc, source_id
            limit :limit
            """;

    private final JdbcClient jdbc;

    public NotificationQueries(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * My latest events since {@code since}, newest first. {@code friendIds}: my active friends,
     * never empty (callers add mine, which the query leaves out).
     */
    public List<NotificationRow> latest(
            UUID memberId, Collection<UUID> friendIds, Instant since, int limit) {
        return jdbc.sql(EVENTS)
                .param("me", memberId)
                .param("friends", friendIds)
                .param("since", Timestamp.from(since))
                .param("excerpt", EXCERPT_LENGTH)
                .param("limit", limit)
                .query(
                        (rs, row) ->
                                new NotificationRow(
                                        NotificationKind.valueOf(rs.getString("kind")),
                                        rs.getObject("source_id", UUID.class),
                                        rs.getTimestamp("at").toInstant(),
                                        rs.getObject("actor_id", UUID.class),
                                        rs.getObject("session_id", UUID.class),
                                        rs.getObject("session_owner_id", UUID.class),
                                        Optional.ofNullable(rs.getDate("climbed_on"))
                                                .map(java.sql.Date::toLocalDate)
                                                .orElse(null),
                                        rs.getString("reaction"),
                                        rs.getString("excerpt")))
                .list();
    }

    /** When I last opened the bell, if ever. */
    public Optional<Instant> seenAt(UUID memberId) {
        return jdbc.sql("select seen_at from notification_seen where member_id = :me")
                .param("me", memberId)
                .query((rs, row) -> rs.getTimestamp("seen_at").toInstant())
                .optional();
    }

    /** I opened the bell now; never moves back (two tabs, two clocks). */
    public void markSeen(UUID memberId, Instant at) {
        jdbc.sql(
                        "insert into notification_seen (member_id, seen_at) values (:me, :at)"
                                + " on conflict (member_id) do update set seen_at ="
                                + " greatest(notification_seen.seen_at, excluded.seen_at)")
                .param("me", memberId)
                .param("at", Timestamp.from(at))
                .update();
    }
}
