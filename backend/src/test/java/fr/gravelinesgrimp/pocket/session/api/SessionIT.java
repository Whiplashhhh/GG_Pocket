package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.AscentDraft;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

/** Sessions: what a climber logs, and what nobody else can read or change. */
@IntegrationTest
class SessionIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private ClimbingSessionRepository sessions;
    @Autowired private Clock clock;

    @Test
    void aClimberLogsASessionAndGetsItsFigures() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var route = routes.create(setter);
        var other = routes.create(setter, RouteStatus.LAST_CHANCE);

        create(
                        me,
                        UUID.randomUUID(),
                        today(),
                        ascent(route, "LEAD", true, true, 0),
                        ascent(route, "TOP_ROPE", false, false, 2),
                        ascent(other, "TOP_ROPE", true, false, 1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.climbedOn").value(today().toString()))
                .andExpect(jsonPath("$.visibility").value("FRIENDS"))
                .andExpect(jsonPath("$.feeling").value(8))
                .andExpect(jsonPath("$.comment").value("Les avant-bras en feu"))
                .andExpect(jsonPath("$.editable").value(true))
                .andExpect(jsonPath("$.photoUrl").doesNotExist())
                .andExpect(jsonPath("$.summary.ascentCount").value(3))
                .andExpect(jsonPath("$.summary.toppedCount").value(2))
                .andExpect(jsonPath("$.summary.leadCount").value(1))
                .andExpect(jsonPath("$.summary.maxGrade").value("6a"))
                .andExpect(
                        jsonPath("$.ascents[*].route.id")
                                .value(
                                        contains(
                                                route.id().toString(),
                                                route.id().toString(),
                                                other.id().toString())))
                .andExpect(jsonPath("$.ascents[1].falls").value(2))
                .andExpect(jsonPath("$.ascents[0].clean").value(true));
    }

    @Test
    void sendingTheSameSessionTwiceKeepsOneCopy() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var clientRef = UUID.randomUUID();

        var id = idOf(create(me, clientRef, today(), ascent(route, "LEAD", true, true, 0)));
        create(me, clientRef, today(), ascent(route, "TOP_ROPE", false, false, 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ascents[0].mode").value("LEAD"));
        // Another climber may draw the same reference: it is per climber.
        create(
                        members.create(Role.CLIMBER),
                        clientRef,
                        today(),
                        ascent(route, "LEAD", true, true, 0))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/me/sessions").with(as(me)))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void nobodyElseReadsChangesOrDeletesMySession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));

        for (var intruder : List.of(members.create(Role.CLIMBER), members.create(Role.ADMIN))) {
            mockMvc.perform(get("/api/v1/sessions/" + id).with(as(intruder)))
                    .andExpect(status().isNotFound());
            mockMvc.perform(
                            put("/api/v1/sessions/" + id)
                                    .with(as(intruder))
                                    .with(xsrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            body(
                                                    UUID.randomUUID(),
                                                    today(),
                                                    ascent(route, "LEAD", false, false, 3))))
                    .andExpect(status().isNotFound());
            mockMvc.perform(delete("/api/v1/sessions/" + id).with(as(intruder)).with(xsrf()))
                    .andExpect(status().isNotFound());
            mockMvc.perform(
                            delete("/api/v1/sessions/" + id + "/photo")
                                    .with(as(intruder))
                                    .with(xsrf()))
                    .andExpect(status().isNotFound());
            upload(intruder, id, png()).andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/me/sessions").with(as(intruder)))
                    .andExpect(jsonPath("$.items", empty()));
        }
        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ascents[0].clean").value(true));
    }

    @Test
    void theDateIsTodayOrUpToAWeekBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var lead = ascent(route, "LEAD", true, true, 0);

        create(me, UUID.randomUUID(), today().plusDays(1), lead)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATE_IN_THE_FUTURE"));
        create(me, UUID.randomUUID(), today().minusDays(8), lead)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SESSION_TOO_OLD"));
        create(me, UUID.randomUUID(), today().minusDays(7), lead).andExpect(status().isCreated());
    }

    @Test
    void invalidSessionsAreRefused() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));

        create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", false, true, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ASCENT"));
        create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ASCENT"));
        create(me, UUID.randomUUID(), today())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        create(me, UUID.randomUUID(), today(), ascent(route, "BOULDER", true, true, 0))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                        post("/api/v1/sessions")
                                .with(as(me))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"clientRef": "%s", "climbedOn": "%s", "visibility": "FRIENDS",
                                         "ascents": [{"routeId": "%s", "mode": "LEAD", "topped": true,
                                         "clean": true, "falls": 0, "firstTry": false}]}
                                        """
                                                .formatted(
                                                        UUID.randomUUID(),
                                                        today(),
                                                        UUID.randomUUID())))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/me/sessions").with(as(me)))
                .andExpect(jsonPath("$.items", empty()));
    }

    @Test
    void aDismountedRouteIsFineOnlyIfItWasStillOnTheWallThatDay() throws Exception {
        var me = members.create(Role.CLIMBER);
        // Dismounted three days ago.
        var gone = routes.create(members.create(Role.SETTER), RouteStatus.DISMOUNTED);

        create(me, UUID.randomUUID(), today(), ascent(gone, "TOP_ROPE", true, false, 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
        create(
                        me,
                        UUID.randomUUID(),
                        today().minusDays(4),
                        ascent(gone, "TOP_ROPE", true, false, 1))
                .andExpect(status().isCreated());
    }

    @Test
    void aFrozenMonthCanNoLongerChange() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var old = oldSession(me, route);

        mockMvc.perform(get("/api/v1/sessions/" + old.id()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editable").value(false));
        mockMvc.perform(
                        put("/api/v1/sessions/" + old.id())
                                .with(as(me))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        body(
                                                UUID.randomUUID(),
                                                old.climbedOn(),
                                                ascent(route, "LEAD", true, true, 0))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MONTH_FROZEN"));
        mockMvc.perform(delete("/api/v1/sessions/" + old.id()).with(as(me)).with(xsrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MONTH_FROZEN"));
        upload(me, old.id().toString(), png())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MONTH_FROZEN"));
    }

    @Test
    void changingTheDateFollowsTheRulesOfANewSession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));

        update(me, id, today().minusDays(9), ascent(route, "LEAD", true, true, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SESSION_TOO_OLD"));
        update(
                        me,
                        id,
                        today().minusDays(2),
                        ascent(route, "TOP_ROPE", true, false, 1),
                        ascent(route, "LEAD", false, false, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.climbedOn").value(today().minusDays(2).toString()))
                .andExpect(jsonPath("$.ascents", hasSize(2)))
                .andExpect(jsonPath("$.ascents[1].falls").value(4));
        update(me, id, today().minusDays(2), ascent(route, "LEAD", true, false, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ascents", hasSize(1)));
    }

    @Test
    void aCleanAscentTicksJeSaisLaFaireUntilItIsGone() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var routePath = "/api/v1/routes/" + route.id();

        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));
        mockMvc.perform(get(routePath).with(as(me)))
                .andExpect(jsonPath("$.route.checks").value(contains("LEAD")));
        mockMvc.perform(delete(routePath + "/checks/LEAD").with(as(me)).with(xsrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHECK_FROM_ASCENT"));

        // A hand-ticked mark survives the sessions.
        mockMvc.perform(put(routePath + "/checks/TOP_ROPE").with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        update(me, id, today(), ascent(route, "LEAD", true, false, 2)).andExpect(status().isOk());
        mockMvc.perform(get(routePath).with(as(me)))
                .andExpect(jsonPath("$.route.checks").value(contains("TOP_ROPE")));

        update(me, id, today(), ascent(route, "TOP_ROPE", true, true, 0))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/sessions/" + id).with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(routePath).with(as(me)))
                .andExpect(jsonPath("$.route.checks").value(contains("TOP_ROPE")));
        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(status().isNotFound());
    }

    @Test
    void theRouteSheetCountsEveryonesAscentsAndShowsMine() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var routePath = "/api/v1/routes/" + route.id();

        mockMvc.perform(get(routePath).with(as(me)))
                .andExpect(jsonPath("$.stats.ascentCount").value(0))
                .andExpect(jsonPath("$.stats.leadPercent").doesNotExist())
                .andExpect(jsonPath("$.myAscents", empty()));

        var mine =
                idOf(
                        create(
                                me,
                                UUID.randomUUID(),
                                today().minusDays(1),
                                ascent(route, "TOP_ROPE", false, false, 3),
                                ascent(route, "LEAD", true, false, 1)));
        create(friend, UUID.randomUUID(), today(), ascent(route, "TOP_ROPE", true, true, 0))
                .andExpect(status().isCreated());

        mockMvc.perform(get(routePath).with(as(me)))
                .andExpect(jsonPath("$.stats.ascentCount").value(3))
                .andExpect(jsonPath("$.stats.toppedCount").value(2))
                .andExpect(jsonPath("$.stats.climberCount").value(2))
                .andExpect(jsonPath("$.stats.leadPercent").value(33))
                .andExpect(jsonPath("$.route.toppedCount").value(2))
                .andExpect(jsonPath("$.myAscents", hasSize(2)))
                .andExpect(jsonPath("$.myAscents[0].mode").value("LEAD"))
                .andExpect(jsonPath("$.myAscents[0].sessionId").value(mine))
                .andExpect(jsonPath("$.myAscents[1].falls").value(3));
        mockMvc.perform(get("/api/v1/routes").with(as(friend)))
                .andExpect(
                        jsonPath("$[?(@.id == '" + route.id() + "')].toppedCount")
                                .value(contains(2)));
        mockMvc.perform(get("/api/v1/me/climbed-routes").with(as(me)))
                .andExpect(jsonPath("$[0].routeId").value(route.id().toString()))
                .andExpect(jsonPath("$[0].lastClimbedOn").value(today().minusDays(1).toString()));
    }

    @Test
    void mySessionsComePageByPageLatestDayFirst() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var older =
                idOf(
                        create(
                                me,
                                UUID.randomUUID(),
                                today().minusDays(3),
                                ascent(route, "LEAD", true, true, 0)));
        var first =
                idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));
        var second =
                idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));

        var page =
                mockMvc.perform(get("/api/v1/me/sessions?size=2").with(as(me)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items[*].id").value(contains(second, first)))
                        .andReturn();
        String cursor = JsonPath.read(page.getResponse().getContentAsString(), "$.nextCursor");
        mockMvc.perform(get("/api/v1/me/sessions?size=2&cursor=" + cursor).with(as(me)))
                .andExpect(jsonPath("$.items[*].id").value(contains(older)))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
        mockMvc.perform(get("/api/v1/me/sessions?cursor=nimporte-quoi").with(as(me)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void theSessionPhotoIsForItsClimberOnly() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));

        var uploaded =
                upload(me, id, png())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.photoUrl").exists())
                        .andReturn();
        String photoUrl = JsonPath.read(uploaded.getResponse().getContentAsString(), "$.photoUrl");
        mockMvc.perform(get(photoUrl).with(as(me))).andExpect(status().isOk());
        mockMvc.perform(get(photoUrl).with(as(members.create(Role.ADMIN))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/sessions/" + id).with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(photoUrl).with(as(me))).andExpect(status().isNotFound());
    }

    @Test
    void removingThePhotoKeepsTheSession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true, 0)));
        upload(me, id, png()).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/sessions/" + id + "/photo").with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").doesNotExist());
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }

    /** A session three months back: its month is frozen, whatever the day of the run. */
    private ClimbingSession oldSession(Member me, Route route) {
        var draft =
                new SessionDraft(
                        today().minusMonths(3),
                        null,
                        null,
                        SessionVisibility.FRIENDS,
                        List.of(
                                new AscentDraft(
                                        route.id(), ClimbMode.LEAD, true, true, 0, true, null)));
        return sessions.saveAndFlush(new ClimbingSession(me.id(), UUID.randomUUID(), draft));
    }

    private static String ascent(
            Route route, String mode, boolean topped, boolean clean, int falls) {
        return """
                {"routeId": "%s", "mode": "%s", "topped": %s, "clean": %s, "falls": %d,
                 "firstTry": false, "feeling": 6}
                """
                .formatted(route.id(), mode, topped, clean, falls);
    }

    private static String body(UUID clientRef, LocalDate day, String... ascents) {
        return """
                {"clientRef": "%s", "climbedOn": "%s", "feeling": 8,
                 "comment": "  Les avant-bras en feu ", "visibility": "FRIENDS", "ascents": [%s]}
                """
                .formatted(clientRef, day, String.join(",", ascents));
    }

    private ResultActions create(Member me, UUID clientRef, LocalDate day, String... ascents)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(clientRef, day, ascents)));
    }

    private ResultActions update(Member me, String id, LocalDate day, String... ascents)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + id)
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), day, ascents)));
    }

    private static String idOf(ResultActions result) throws Exception {
        return JsonPath.read(
                result.andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
    }

    private ResultActions upload(Member me, String id, MockMultipartFile file) throws Exception {
        var request = multipart(HttpMethod.PUT, "/api/v1/sessions/" + id + "/photo").file(file);
        request.with(as(me)).with(xsrf());
        return mockMvc.perform(request);
    }

    private static MockMultipartFile png() throws Exception {
        var image = new BufferedImage(64, 48, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(new Color(0xE8, 0x6A, 0x1F));
        graphics.fillRect(0, 0, 64, 48);
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "seance.png", "image/png", output.toByteArray());
    }
}
