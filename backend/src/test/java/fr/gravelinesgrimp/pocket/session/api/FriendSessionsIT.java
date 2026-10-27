package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;
import static fr.gravelinesgrimp.pocket.SessionFixtures.tried;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import javax.imageio.ImageIO;

/**
 * Friends read the sessions shared with friends (spec §8.8, §14): one by one, page by page, as a
 * logbook, photo included. Never the "juste moi" ones, never someone else's, never to change them.
 */
@IntegrationTest
class FriendSessionsIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private MemberRepository memberRepository;
    @Autowired private Clock clock;

    @Test
    void aFriendReadsMySharedSessionsButNotTheOthers() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var route = routes.create(members.create(Role.SETTER));
        var shared = sessions.log(me, today(), 7, clean(route, ClimbMode.LEAD));
        var hidden =
                sessions.log(
                        me,
                        today(),
                        4,
                        SessionVisibility.PRIVATE,
                        tried(route, ClimbMode.TOP_ROPE));

        mockMvc.perform(get("/api/v1/sessions/" + shared.id()).with(as(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(me.id().toString()))
                .andExpect(jsonPath("$.editable").value(false));
        mockMvc.perform(get("/api/v1/sessions/" + hidden.id()).with(as(friend)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/sessions").with(as(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id").value(contains(shared.id().toString())));
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/logbook").with(as(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.sessionCount").value(1))
                .andExpect(jsonPath("$.totals.ascentCount").value(1));

        // My own view still holds both.
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/logbook").with(as(me)))
                .andExpect(jsonPath("$.totals.sessionCount").value(2));
    }

    @Test
    void noFriendshipNoSessions() throws Exception {
        var me = members.create(Role.CLIMBER);
        var asking = members.create(Role.CLIMBER);
        friendships.ask(asking, me);
        var stranger = members.create(Role.CLIMBER);
        var admin = members.create(Role.ADMIN);
        var route = routes.create(members.create(Role.SETTER));
        var shared = sessions.log(me, today(), 7, clean(route, ClimbMode.LEAD));

        // A pending request opens nothing, and neither does a role.
        for (var reader : new Member[] {asking, stranger, admin}) {
            mockMvc.perform(get("/api/v1/sessions/" + shared.id()).with(as(reader)))
                    .andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/members/" + me.id() + "/sessions").with(as(reader)))
                    .andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/members/" + me.id() + "/logbook").with(as(reader)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void aFriendNeverChangesOrDeletesMySession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var route = routes.create(members.create(Role.SETTER));
        var shared = sessions.log(me, today(), 7, clean(route, ClimbMode.LEAD));

        mockMvc.perform(delete("/api/v1/sessions/" + shared.id()).with(as(friend)).with(xsrf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(
                        delete("/api/v1/sessions/" + shared.id() + "/photo")
                                .with(as(friend))
                                .with(xsrf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/sessions/" + shared.id()).with(as(me)))
                .andExpect(status().isOk());
    }

    @Test
    void aDisabledClimberIsReadByNobodyButThemselves() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var route = routes.create(members.create(Role.SETTER));
        var shared = sessions.log(me, today(), 7, clean(route, ClimbMode.LEAD));
        me.disable();
        memberRepository.saveAndFlush(me);

        mockMvc.perform(get("/api/v1/sessions/" + shared.id()).with(as(friend)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/sessions").with(as(friend)))
                .andExpect(status().isNotFound());
    }

    @Test
    void theSessionPhotoFollowsTheSession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var stranger = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var created =
                mockMvc.perform(
                                post("/api/v1/sessions")
                                        .with(as(me))
                                        .with(xsrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"clientRef": "%s", "climbedOn": "%s",
                                                 "visibility": "FRIENDS", "ascents": [
                                                 {"routeId": "%s", "mode": "LEAD", "topped": true,
                                                  "clean": true, "falls": 0, "firstTry": false}]}
                                                """
                                                        .formatted(
                                                                UUID.randomUUID(),
                                                                today(),
                                                                route.id())))
                        .andExpect(status().isCreated())
                        .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var upload = multipart(HttpMethod.PUT, "/api/v1/sessions/" + id + "/photo").file(png());
        upload.with(as(me)).with(xsrf());
        var uploaded = mockMvc.perform(upload).andExpect(status().isOk()).andReturn();
        String photoUrl = JsonPath.read(uploaded.getResponse().getContentAsString(), "$.photoUrl");

        mockMvc.perform(get(photoUrl).with(as(friend))).andExpect(status().isOk());
        mockMvc.perform(get(photoUrl).with(as(stranger))).andExpect(status().isNotFound());
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }

    private static MockMultipartFile png() throws Exception {
        var image = new BufferedImage(64, 48, BufferedImage.TYPE_INT_RGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "seance.png", "image/png", output.toByteArray());
    }
}
