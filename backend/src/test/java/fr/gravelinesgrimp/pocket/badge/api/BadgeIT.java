package fr.gravelinesgrimp.pocket.badge.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import fr.gravelinesgrimp.pocket.topo.domain.Route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/** The badges follow every change of the sessions, through the real endpoints (spec §9.3, §9.5). */
@IntegrationTest
class BadgeIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;

    @Test
    void aFirstSessionUnlocksItsBadgesAndKeepsThem() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER), "6a");

        var id =
                idOf(
                        create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true))
                                .andExpect(
                                        jsonPath(
                                                "$.unlockedBadges",
                                                contains(
                                                        "TOPPED_1",
                                                        "LEAD_1",
                                                        "GRADE_5B",
                                                        "GRADE_5C",
                                                        "GRADE_6A",
                                                        "FLASH")))
                                .andExpect(jsonPath("$.badges", hasItem("TOPPED_1"))));

        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badges", hasItem("FLASH")))
                .andExpect(jsonPath("$.unlockedBadges", empty()));
        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badges[0].code").value("TOPPED_1"))
                .andExpect(jsonPath("$.badges[0].family").value("TOPPED"))
                .andExpect(jsonPath("$.badges[0].repeatable").value(false))
                .andExpect(jsonPath("$.badges[0].count").value(1))
                .andExpect(jsonPath("$.badges[0].firstEarnedOn").value(today().toString()))
                .andExpect(jsonPath("$.badges[1].code").value("TOPPED_5"))
                .andExpect(jsonPath("$.badges[1].count").value(0))
                .andExpect(jsonPath("$.badges[?(@.code == 'FLASH')].count").value(contains(1)))
                .andExpect(
                        jsonPath("$.badges[?(@.code == 'FLASH')].repeatable")
                                .value(contains(true)));
    }

    @Test
    void sendingTheSameSessionAgainUnlocksNothing() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var clientRef = UUID.randomUUID();
        create(me, clientRef, today(), ascent(route, "TOP_ROPE", true, false))
                .andExpect(status().isCreated());

        create(me, clientRef, today(), ascent(route, "TOP_ROPE", true, false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unlockedBadges", empty()))
                .andExpect(jsonPath("$.badges", hasItem("TOPPED_1")));
    }

    @Test
    void aBadgeWhoseConditionNoLongerHoldsIsTakenBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, false)));

        update(me, id, today(), ascent(route, "LEAD", false, false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badges", not(hasItem("TOPPED_1"))))
                .andExpect(jsonPath("$.unlockedBadges", empty()));

        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.badges[0].code").value("TOPPED_1"))
                .andExpect(jsonPath("$.badges[0].count").value(0))
                .andExpect(jsonPath("$.badges[0].firstEarnedOn").doesNotExist());
    }

    @Test
    void deletingASessionHandsItsBadgeToTheNextOne() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var first =
                idOf(
                        create(
                                me,
                                UUID.randomUUID(),
                                today(),
                                ascent(route, "TOP_ROPE", true, false)));
        var second =
                idOf(
                        create(
                                        me,
                                        UUID.randomUUID(),
                                        today(),
                                        ascent(route, "TOP_ROPE", true, false))
                                .andExpect(jsonPath("$.unlockedBadges", not(hasItem("TOPPED_1")))));

        mockMvc.perform(delete("/api/v1/sessions/" + first).with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/sessions/" + second).with(as(me)))
                .andExpect(jsonPath("$.badges", hasItem("TOPPED_1")));
        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.badges[0].count").value(1));
    }

    @Test
    void deletingTheOnlySessionTakesEverythingBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true)));

        mockMvc.perform(delete("/api/v1/sessions/" + id).with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.badges[?(@.count > 0)]", empty()));
    }

    @Test
    void anyClimberReadsTheLibraryOfAnActiveMember() throws Exception {
        var anna = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        create(anna, UUID.randomUUID(), today(), ascent(route, "TOP_ROPE", true, false))
                .andExpect(status().isCreated());
        var stranger = members.create(Role.CLIMBER);

        mockMvc.perform(get("/api/v1/members/" + anna.id() + "/badges").with(as(stranger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badges[0].count").value(1));
        mockMvc.perform(get("/api/v1/members/" + UUID.randomUUID() + "/badges").with(as(stranger)))
                .andExpect(status().isNotFound());
    }

    @Test
    void theSeniorityShowsOnProfilesAndInTheDirectory() throws Exception {
        var anna = members.create(Role.CLIMBER);
        var year = ClubTime.today(clock).getYear();
        jdbc.update("update member set club_arrival_year = ? where id = ?", year - 5, anna.id());
        var reader = members.create(Role.CLIMBER);

        mockMvc.perform(get("/api/v1/me").with(as(anna)))
                .andExpect(jsonPath("$.seniority").value("FORCER"));
        mockMvc.perform(get("/api/v1/members/" + anna.id()).with(as(reader)))
                .andExpect(jsonPath("$.member.seniority").value("FORCER"));
        mockMvc.perform(get("/api/v1/members").with(as(reader)))
                .andExpect(
                        jsonPath(
                                        "$.members[?(@.member.id == '"
                                                + anna.id()
                                                + "')].member.seniority")
                                .value(contains("FORCER")));
        mockMvc.perform(get("/api/v1/members/" + reader.id()).with(as(anna)))
                .andExpect(jsonPath("$.member.seniority").doesNotExist());
    }

    @Test
    void myLibraryTellsWhereIStandOnTheCountedTiers() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, false))
                .andExpect(status().isCreated());
        create(me, UUID.randomUUID(), today(), ascent(route, "TOP_ROPE", true, false))
                .andExpect(status().isCreated());
        var reader = members.create(Role.CLIMBER);

        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.progress.topped").value(2))
                .andExpect(jsonPath("$.progress.lead").value(1))
                .andExpect(jsonPath("$.progress.climbingDays").value(1));
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/badges").with(as(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").doesNotExist());
    }

    @Test
    void myFavouriteBadgesShowNextToMyNameAtTheirLevel() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER), "6a");
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true)));

        chooseFavorites(me, "[\"FIRST_GRADE\", \"FLASH\"]")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorites", contains("FIRST_GRADE", "FLASH")));

        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(jsonPath("$.authorBadges[0].badge").value("FIRST_GRADE"))
                .andExpect(jsonPath("$.authorBadges[0].level").value(3))
                .andExpect(jsonPath("$.authorBadges[1].badge").value("FLASH"))
                .andExpect(jsonPath("$.authorBadges[1].level").value(1));
        mockMvc.perform(get("/api/v1/members/" + me.id() + "/badges").with(as(me)))
                .andExpect(jsonPath("$.favorites", contains("FIRST_GRADE", "FLASH")));

        chooseFavorites(me, "[]").andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/sessions/" + id).with(as(me)))
                .andExpect(jsonPath("$.authorBadges", empty()));
    }

    @Test
    void aFavouriteMustBeEarnedAndThreeAtMost() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER), "6a");
        create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, true))
                .andExpect(status().isCreated());

        chooseFavorites(me, "[\"COMEBACK\"]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        chooseFavorites(me, "[\"TOPPED\", \"TOPPED\"]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        chooseFavorites(me, "[\"TOPPED\", \"LEAD\", \"FLASH\", \"FIRST_GRADE\"]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.favorites", empty()));
    }

    @Test
    void aFavouriteLostWithItsSessionIsNoLongerShown() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var id = idOf(create(me, UUID.randomUUID(), today(), ascent(route, "LEAD", true, false)));
        chooseFavorites(me, "[\"LEAD\"]").andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/sessions/" + id).with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/badges").with(as(me)))
                .andExpect(jsonPath("$.favorites", empty()));
    }

    // --- helpers ---

    private ResultActions chooseFavorites(Member me, String badges) throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/badges/favorites")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"badges\": " + badges + "}"));
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }

    private static String ascent(Route route, String mode, boolean topped, boolean clean) {
        return """
                {"routeId": "%s", "mode": "%s", "topped": %s, "clean": %s, "falls": %d,
                 "firstTry": %s}
                """
                .formatted(route.id(), mode, topped, clean, topped ? 0 : 1, clean);
    }

    private static String body(UUID clientRef, LocalDate day, String ascent) {
        return """
                {"clientRef": "%s", "climbedOn": "%s", "visibility": "FRIENDS", "ascents": [%s]}
                """
                .formatted(clientRef, day, ascent);
    }

    private ResultActions create(Member me, UUID clientRef, LocalDate day, String ascent)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(clientRef, day, ascent)));
    }

    private ResultActions update(Member me, String id, LocalDate day, String ascent)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + id)
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), day, ascent)));
    }

    private static String idOf(ResultActions result) throws Exception {
        return JsonPath.read(
                result.andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
    }
}
