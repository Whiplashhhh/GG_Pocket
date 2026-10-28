package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.time.LocalDate;

/**
 * Comments on sessions (spec §8.2): whoever reads the session comments on it; the author or the
 * board deletes a comment, the board's deletions go to the audit log.
 */
@IntegrationTest
class SessionCommentIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void friendsAndTheClimberTalkUnderTheSession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var session = shared(me);

        comment(friend, session, "  Propre !  ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Propre !"))
                .andExpect(jsonPath("$.author.nickname").value(friend.nickname()))
                .andExpect(jsonPath("$.deletable").value(true));
        comment(me, session, "Merci !").andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/sessions/" + session.id() + "/comments").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].body").value("Propre !"))
                .andExpect(jsonPath("$[0].deletable").value(false))
                .andExpect(jsonPath("$[1].body").value("Merci !"))
                .andExpect(jsonPath("$[1].deletable").value(true));
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(friend)))
                .andExpect(jsonPath("$.commentCount").value(2))
                .andExpect(jsonPath("$.author.nickname").value(me.nickname()));
    }

    @Test
    void noCommentWhereIDoNotRead() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var asking = members.create(Role.CLIMBER);
        friendships.ask(asking, me);
        var board = members.create(Role.BOARD);
        var shared = shared(me);
        var hidden =
                sessions.log(
                        me,
                        today(),
                        6,
                        SessionVisibility.PRIVATE,
                        clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));

        comment(friend, hidden, "Coucou").andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/sessions/" + hidden.id() + "/comments").with(as(friend)))
                .andExpect(status().isNotFound());
        // A pending request opens nothing, and neither does a role.
        for (var reader : new Member[] {asking, board}) {
            comment(reader, shared, "Coucou").andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/sessions/" + shared.id() + "/comments").with(as(reader)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void anEmptyOrTooLongCommentIsRefused() throws Exception {
        var me = members.create(Role.CLIMBER);
        var session = shared(me);

        comment(me, session, "   ").andExpect(status().isBadRequest());
        comment(me, session, "a".repeat(501)).andExpect(status().isBadRequest());
    }

    @Test
    void onlyTheAuthorOrTheBoardDeletes() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var other = members.create(Role.SETTER);
        friendships.befriend(me, friend);
        friendships.befriend(me, other);
        var session = shared(me);
        var id = idOf(comment(friend, session, "Le mien"));

        // Neither the climber of the session nor another friend, even a setter.
        remove(me, id).andExpect(status().isNotFound());
        remove(other, id).andExpect(status().isNotFound());
        remove(friend, id).andExpect(status().isNoContent());
        remove(friend, id).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/sessions/" + session.id() + "/comments").with(as(me)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void theAuthorStillDeletesAfterTheFriendshipEnds() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var friendship = friendships.befriend(me, friend);
        var session = shared(me);
        var id = idOf(comment(friend, session, "Mes mots"));
        friendships.end(friendship);

        remove(friend, id).andExpect(status().isNoContent());
    }

    @Test
    void theBoardModeratesAndTheAuditLogKeepsTrace() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var board = members.create(Role.BOARD);
        var session = shared(me);
        var id = idOf(comment(friend, session, "Message pas tres sympa."));

        remove(board, id).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/sessions/" + session.id() + "/comments").with(as(me)))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(me)))
                .andExpect(jsonPath("$.commentCount").value(0));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(board)))
                .andExpect(jsonPath("$.entries[0].action").value("SESSION_COMMENT_DELETED"))
                .andExpect(jsonPath("$.entries[0].targetType").value("SESSION_COMMENT"))
                .andExpect(jsonPath("$.entries[0].targetId").value(id))
                .andExpect(
                        jsonPath("$.entries[0].targetLabel")
                                .value(
                                        me.nickname()
                                                + ", "
                                                + session.climbedOn()
                                                + " ("
                                                + friend.nickname()
                                                + ")"))
                .andExpect(jsonPath("$.entries[0].before").value("Message pas tres sympa."));
    }

    private ClimbingSession shared(Member climber) {
        return sessions.log(
                climber,
                today(),
                8,
                clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
    }

    private ResultActions comment(Member author, ClimbingSession session, String body)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions/" + session.id() + "/comments")
                        .with(as(author))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"" + body + "\"}"));
    }

    private ResultActions remove(Member actor, String commentId) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/session-comments/" + commentId).with(as(actor)).with(xsrf()));
    }

    private static String idOf(ResultActions created) throws Exception {
        var body =
                created.andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.id");
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }
}
