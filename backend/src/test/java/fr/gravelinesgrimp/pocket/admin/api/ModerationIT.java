package fr.gravelinesgrimp.pocket.admin.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.assertj.core.api.Assertions.assertThat;
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
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;

/**
 * The moderation list (spec §8.11): the latest comments on routes and sessions together, newest
 * first, page by page, for the board and the admin only; a deleted one leaves it.
 */
@IntegrationTest
class ModerationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void theBoardSeesBothKindsNewestFirstAndDeletesThem() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(climber, friend);
        var board = members.create(Role.BOARD);
        var route = routes.create(members.create(Role.SETTER));
        var session = sessions.log(climber, ClubTime.today(clock), 7, clean(route, ClimbMode.LEAD));

        var onRoute = add(friend, "/api/v1/routes/" + route.id() + "/comments", "Sur la voie");
        var onSession =
                add(friend, "/api/v1/sessions/" + session.id() + "/comments", "Sur la séance");

        mockMvc.perform(get("/api/v1/admin/moderation/comments").with(as(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].id").value(onSession))
                .andExpect(jsonPath("$.comments[0].kind").value("SESSION"))
                .andExpect(jsonPath("$.comments[0].author.nickname").value(friend.nickname()))
                .andExpect(jsonPath("$.comments[0].climber.nickname").value(climber.nickname()))
                .andExpect(
                        jsonPath("$.comments[0].climbedOn").value(session.climbedOn().toString()))
                .andExpect(jsonPath("$.comments[0].route").doesNotExist())
                .andExpect(jsonPath("$.comments[1].id").value(onRoute))
                .andExpect(jsonPath("$.comments[1].kind").value("ROUTE"))
                .andExpect(jsonPath("$.comments[1].route.name").value(route.name()))
                .andExpect(jsonPath("$.comments[1].climber").doesNotExist());

        // Page by page, across both kinds.
        var first =
                mockMvc.perform(
                                get("/api/v1/admin/moderation/comments")
                                        .param("size", "1")
                                        .with(as(board)))
                        .andExpect(jsonPath("$.comments", hasSize(1)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String cursor = JsonPath.read(first, "$.nextCursor");
        mockMvc.perform(
                        get("/api/v1/admin/moderation/comments")
                                .param("size", "1")
                                .param("cursor", cursor)
                                .with(as(board)))
                .andExpect(jsonPath("$.comments[0].id").value(onRoute));

        mockMvc.perform(
                        delete("/api/v1/session-comments/" + onSession)
                                .with(as(board))
                                .with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/route-comments/" + onRoute).with(as(board)).with(xsrf()))
                .andExpect(status().isNoContent());
        var after =
                mockMvc.perform(get("/api/v1/admin/moderation/comments").with(as(board)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        List<String> ids = JsonPath.read(after, "$.comments[*].id");
        assertThat(ids).doesNotContain(onRoute, onSession);
    }

    @Test
    void belowTheBoardTheListIsClosed() throws Exception {
        for (var role : List.of(Role.CLIMBER, Role.SETTER)) {
            mockMvc.perform(get("/api/v1/admin/moderation/comments").with(as(members.create(role))))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(
                        get("/api/v1/admin/moderation/comments")
                                .with(as(members.create(Role.ADMIN))))
                .andExpect(status().isOk());
    }

    private String add(Member author, String path, String body) throws Exception {
        var created =
                mockMvc.perform(
                                post(path)
                                        .with(as(author))
                                        .with(xsrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"body\": \"" + body + "\"}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(created, "$.id");
    }
}
