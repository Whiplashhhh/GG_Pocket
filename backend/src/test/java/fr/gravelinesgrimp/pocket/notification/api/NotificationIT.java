package fr.gravelinesgrimp.pocket.notification.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

/**
 * The bell (D-128): what happened to me, from the tables themselves; what is new since I last
 * opened it; and nothing from a session I can no longer read.
 */
@IntegrationTest
class NotificationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void theBellGathersWhatHappenedToMeAndForgetsItOnceSeen() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var asking = members.create(Role.TOURIST);
        var accepting = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var mine = shared(me, SessionVisibility.FRIENDS);
        var theirs = shared(friend, SessionVisibility.FRIENDS);
        shared(friend, SessionVisibility.PRIVATE);

        react(friend, mine, "FIRE").andExpect(status().isOk());
        comment(friend, mine, "Bravo !", null);
        var question = idOf(comment(me, theirs, "La méthode ?", null));
        comment(friend, theirs, "Talon à droite.", question);
        friendships.ask(asking, me);
        friendships.befriend(me, accepting);

        notifications(me)
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.items[*].kind",
                                containsInAnyOrder(
                                        "REACTION",
                                        "COMMENT",
                                        "FRIEND_SESSION",
                                        "REPLY",
                                        "FRIEND_REQUEST",
                                        "FRIEND_ACCEPTED")))
                .andExpect(jsonPath("$.unreadCount").value(6))
                // Newest first.
                .andExpect(jsonPath("$.items[0].kind").value("FRIEND_ACCEPTED"))
                .andExpect(jsonPath("$.items[0].actor.id").value(accepting.id().toString()))
                .andExpect(
                        jsonPath("$.items[?(@.kind == 'REACTION')].reaction")
                                .value(hasItem("FIRE")))
                .andExpect(
                        jsonPath("$.items[?(@.kind == 'REACTION')].sessionId")
                                .value(hasItem(mine.id().toString())))
                .andExpect(
                        jsonPath("$.items[?(@.kind == 'REPLY')].excerpt")
                                .value(hasItem("Talon à droite.")))
                .andExpect(
                        jsonPath("$.items[?(@.kind == 'FRIEND_SESSION')].sessionId")
                                .value(hasItem(theirs.id().toString())))
                .andExpect(jsonPath("$.items[*].actor.id", not(hasItem(me.id().toString()))));

        mockMvc.perform(put("/api/v1/me/notifications/seen").with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        notifications(me)
                .andExpect(jsonPath("$.unreadCount").value(0))
                .andExpect(jsonPath("$.items", hasSize(6)))
                .andExpect(jsonPath("$.items[*].unread", everyItem(is(false))));

        // A reaction changed afterwards is new again.
        react(friend, mine, "CLAP").andExpect(status().isOk());
        notifications(me)
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.items[0].kind").value("REACTION"))
                .andExpect(jsonPath("$.items[0].unread").value(true));

        // Mine only: someone else's bell says nothing of mine.
        notifications(asking).andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void nothingStaysFromASessionIStoppedReading() throws Exception {
        var me = members.create(Role.CLIMBER);
        var ex = members.create(Role.CLIMBER);
        var friendship = friendships.befriend(ex, me);
        var theirs = shared(ex, SessionVisibility.FRIENDS);
        var question = idOf(comment(me, theirs, "Tu me montres ?", null));
        comment(ex, theirs, "Jeudi.", question);
        notifications(me)
                .andExpect(
                        jsonPath("$.items[*].kind", containsInAnyOrder("FRIEND_SESSION", "REPLY")));

        friendships.end(friendship);

        notifications(me).andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void theBellIsForSignedInMembers() throws Exception {
        mockMvc.perform(get("/api/v1/me/notifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/me/notifications/seen").with(xsrf()))
                .andExpect(status().isUnauthorized());
    }

    private ClimbingSession shared(Member climber, SessionVisibility visibility) {
        return sessions.log(
                climber,
                ClubTime.today(clock),
                8,
                visibility,
                clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
    }

    private ResultActions notifications(Member member) throws Exception {
        return mockMvc.perform(get("/api/v1/me/notifications").with(as(member)));
    }

    private ResultActions react(Member member, ClimbingSession session, String kind)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + session.id() + "/reaction")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\": \"" + kind + "\"}"));
    }

    private ResultActions comment(
            Member author, ClimbingSession session, String body, String replyTo) throws Exception {
        var json =
                replyTo == null
                        ? "{\"body\": \"" + body + "\"}"
                        : "{\"body\": \"" + body + "\", \"replyTo\": \"" + replyTo + "\"}";
        return mockMvc.perform(
                        post("/api/v1/sessions/" + session.id() + "/comments")
                                .with(as(author))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated());
    }

    private static String idOf(ResultActions created) throws Exception {
        var body = created.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.id");
    }
}
