package fr.gravelinesgrimp.pocket.notification.infra;

import fr.gravelinesgrimp.pocket.notification.domain.PushPreferences;
import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The devices that receive push messages, and what their members want (D-128, V27). */
@Repository
public class PushStore {

    private final JdbcClient jdbc;

    public PushStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * This device now receives my messages. A device that belonged to someone else (same phone,
     * other account) changes hands: one endpoint, one member.
     */
    public void subscribe(UUID memberId, PushSubscription device, Instant now) {
        jdbc.sql(
                        """
                        insert into push_subscription
                          (id, member_id, endpoint, p256dh, auth, created_at, updated_at)
                        values (:id, :member, :endpoint, :p256dh, :auth, :now, :now)
                        on conflict (endpoint) do update set member_id = excluded.member_id,
                          p256dh = excluded.p256dh, auth = excluded.auth,
                          updated_at = excluded.updated_at
                        """)
                .param("id", UUID.randomUUID())
                .param("member", memberId)
                .param("endpoint", device.endpoint())
                .param("p256dh", device.p256dh())
                .param("auth", device.auth())
                .param("now", Timestamp.from(now))
                .update();
    }

    /** My latest {@code keep} devices stay; older ones go (a phone replaced, a browser reset). */
    public void keepLatest(UUID memberId, int keep) {
        jdbc.sql(
                        """
                        delete from push_subscription where member_id = :member and id not in
                          (select id from push_subscription where member_id = :member
                           order by updated_at desc limit :keep)
                        """)
                .param("member", memberId)
                .param("keep", keep)
                .update();
    }

    /** Only mine: someone else's device stays where it is. */
    public void unsubscribe(UUID memberId, String endpoint) {
        jdbc.sql("delete from push_subscription where member_id = :member and endpoint = :endpoint")
                .param("member", memberId)
                .param("endpoint", endpoint)
                .update();
    }

    /** The push service said this device is gone. */
    public void forget(String endpoint) {
        jdbc.sql("delete from push_subscription where endpoint = :endpoint")
                .param("endpoint", endpoint)
                .update();
    }

    public List<PushSubscription> devicesOf(UUID memberId) {
        return jdbc.sql(
                        "select endpoint, p256dh, auth from push_subscription"
                                + " where member_id = :member order by updated_at desc")
                .param("member", memberId)
                .query(
                        (rs, row) ->
                                new PushSubscription(
                                        rs.getString("endpoint"),
                                        rs.getString("p256dh"),
                                        rs.getString("auth")))
                .list();
    }

    public boolean hasDevice(UUID memberId, String endpoint) {
        return jdbc.sql(
                                "select count(*) from push_subscription"
                                        + " where member_id = :member and endpoint = :endpoint")
                        .param("member", memberId)
                        .param("endpoint", endpoint)
                        .query(Integer.class)
                        .single()
                > 0;
    }

    public Optional<PushPreferences> preferencesOf(UUID memberId) {
        return jdbc.sql(
                        "select comments, replies, reactions, friend_sessions, friend_requests"
                                + " from push_preference where member_id = :member")
                .param("member", memberId)
                .query(
                        (rs, row) ->
                                new PushPreferences(
                                        rs.getBoolean("comments"),
                                        rs.getBoolean("replies"),
                                        rs.getBoolean("reactions"),
                                        rs.getBoolean("friend_sessions"),
                                        rs.getBoolean("friend_requests")))
                .optional();
    }

    public void savePreferences(UUID memberId, PushPreferences preferences) {
        jdbc.sql(
                        """
                        insert into push_preference
                          (member_id, comments, replies, reactions, friend_sessions, friend_requests)
                        values (:member, :comments, :replies, :reactions, :friendSessions,
                          :friendRequests)
                        on conflict (member_id) do update set comments = excluded.comments,
                          replies = excluded.replies, reactions = excluded.reactions,
                          friend_sessions = excluded.friend_sessions,
                          friend_requests = excluded.friend_requests
                        """)
                .param("member", memberId)
                .param("comments", preferences.comments())
                .param("replies", preferences.replies())
                .param("reactions", preferences.reactions())
                .param("friendSessions", preferences.friendSessions())
                .param("friendRequests", preferences.friendRequests())
                .update();
    }
}
