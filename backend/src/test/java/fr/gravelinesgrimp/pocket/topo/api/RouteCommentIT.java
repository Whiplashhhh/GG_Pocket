package fr.gravelinesgrimp.pocket.topo.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

/** Comments on routes: everyone writes and deletes their own, the board moderates. */
@IntegrationTest
class RouteCommentIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;

    @Test
    void membersCommentAndReadOldestFirst() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var lou = members.create(Role.CLIMBER);
        var sam = members.create(Role.CLIMBER);

        comment(lou, route, "  Le crux est en haut, garde des bras.  ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Le crux est en haut, garde des bras."))
                .andExpect(jsonPath("$.author.nickname").value(lou.nickname()))
                .andExpect(jsonPath("$.deletable").value(true));
        comment(sam, route, "Validee en tete, merci l'ouvreur !").andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/routes/" + route.id() + "/comments").with(as(sam)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].author.nickname")
                                .value(contains(lou.nickname(), sam.nickname())))
                .andExpect(jsonPath("$[*].deletable").value(contains(false, true)));
        mockMvc.perform(get("/api/v1/routes/" + route.id()).with(as(sam)))
                .andExpect(jsonPath("$.route.commentCount").value(2));
    }

    @Test
    void emptyOrTooLongCommentsAreRefused() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var me = members.create(Role.CLIMBER);
        comment(me, route, "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        comment(me, route, "a".repeat(501))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aDismountedRouteTakesNoNewComment() throws Exception {
        var route = routes.create(members.create(Role.SETTER), RouteStatus.DISMOUNTED);
        comment(members.create(Role.CLIMBER), route, "Trop tard ?")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
    }

    @Test
    void aChattyMemberIsSlowedDownAfter20CommentsAnHour() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var me = members.create(Role.CLIMBER);
        for (var i = 1; i <= 20; i++) {
            comment(me, route, "Bavardage " + i).andExpect(status().isCreated());
        }
        comment(me, route, "Encore un")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void iDeleteMyOwnComment() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var me = members.create(Role.CLIMBER);
        var id = idOf(comment(me, route, "Oups, mauvaise voie."));

        remove(me, id).andExpect(status().isNoContent());
        remove(me, id).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/routes/" + route.id() + "/comments").with(as(me)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void aClimberCannotDeleteSomeoneElsesComment() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var author = members.create(Role.CLIMBER);
        var id = idOf(comment(author, route, "Cette voie est un bijou."));

        remove(members.create(Role.CLIMBER), id)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        remove(members.create(Role.SETTER), id).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/routes/" + route.id() + "/comments").with(as(author)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void theBoardModeratesAndTheAuditLogKeepsTrace() throws Exception {
        var route = routes.create(members.create(Role.SETTER));
        var author = members.create(Role.CLIMBER);
        var board = members.create(Role.BOARD);
        var id = idOf(comment(author, route, "Message pas tres sympa."));

        mockMvc.perform(get("/api/v1/routes/" + route.id() + "/comments").with(as(board)))
                .andExpect(jsonPath("$[0].deletable").value(true));
        remove(board, id).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/routes/" + route.id() + "/comments").with(as(author)))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(board)))
                .andExpect(jsonPath("$.entries[0].action").value("ROUTE_COMMENT_DELETED"))
                .andExpect(jsonPath("$.entries[0].targetType").value("ROUTE_COMMENT"))
                .andExpect(jsonPath("$.entries[0].targetId").value(id))
                .andExpect(jsonPath("$.entries[0].before").value("Message pas tres sympa."));
    }

    private ResultActions comment(Member author, Route route, String body) throws Exception {
        return mockMvc.perform(
                post("/api/v1/routes/" + route.id() + "/comments")
                        .with(as(author))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"" + body + "\"}"));
    }

    private ResultActions remove(Member actor, String commentId) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/route-comments/" + commentId).with(as(actor)).with(xsrf()));
    }

    private static String idOf(ResultActions created) throws Exception {
        var body =
                created.andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.id");
    }
}
