package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.belayed;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The belayer, route by route (D-101): any active member but the climber; naming them gives them no
 * right on the session; they see where they held the rope and may take their name back.
 */
@IntegrationTest
class BelayIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private MemberRepository memberRepository;
    @Autowired private Clock clock;

    @Test
    void aClimberNamesTheirBelayerRouteByRoute() throws Exception {
        var me = members.create(Role.CLIMBER);
        // Not a friend in the app: anyone in the club may hold the rope.
        var belayer = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        var first = route();
        var second = route();

        var created =
                save(me, ascent(first, belayer.id()), ascent(second, null))
                        .andExpect(status().isCreated())
                        .andExpect(
                                jsonPath("$.ascents[0].belayer.nickname").value(belayer.nickname()))
                        .andExpect(
                                jsonPath("$.ascents[0].belayer.id").value(belayer.id().toString()))
                        .andExpect(jsonPath("$.ascents[1].belayer").doesNotExist())
                        .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(
                        put("/api/v1/sessions/" + id)
                                .with(as(me))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        body(
                                                UUID.randomUUID(),
                                                ascent(first, other.id()),
                                                ascent(second, other.id()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ascents[0].belayer.nickname").value(other.nickname()))
                .andExpect(jsonPath("$.ascents[1].belayer.nickname").value(other.nickname()));
    }

    @Test
    void nobodyBelaysThemselfNorAnUnknownOrDisabledMember() throws Exception {
        var me = members.create(Role.CLIMBER);
        var disabled = members.create(Role.CLIMBER);
        disabled.disable();
        memberRepository.saveAndFlush(disabled);
        var route = route();

        for (var belayerId : new UUID[] {me.id(), UUID.randomUUID(), disabled.id()}) {
            save(me, ascent(route, belayerId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }

    @Test
    void anAccountAwaitingValidationNeitherNamesNorIsNamedAsABelayer() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var newcomer = members.createAwaitingValidation();
        var route = route();

        save(climber, ascent(route, newcomer.id()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        save(newcomer, ascent(route, climber.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_AWAITING_VALIDATION"));
        save(newcomer, ascent(route, null)).andExpect(status().isCreated());
    }

    @Test
    void aDisabledBelayerDropsOutOfSight() throws Exception {
        var me = members.create(Role.CLIMBER);
        var belayer = members.create(Role.CLIMBER);
        var session = sessions.log(me, today(), 7, belayed(route(), ClimbMode.LEAD, belayer));
        belayer.disable();
        memberRepository.saveAndFlush(belayer);

        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ascents[0].belayer").doesNotExist());
    }

    @Test
    void theBelayerSeesWhereTheyHeldTheRopeAndNothingMore() throws Exception {
        var belayer = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var stranger = members.create(Role.CLIMBER);
        friendships.befriend(friend, belayer);
        var shared =
                sessions.log(
                        friend,
                        today(),
                        7,
                        belayed(route(), ClimbMode.LEAD, belayer),
                        clean(route(), ClimbMode.LEAD),
                        belayed(route(), ClimbMode.TOP_ROPE, belayer));
        var hidden =
                sessions.log(
                        friend,
                        today().minusDays(1),
                        7,
                        SessionVisibility.PRIVATE,
                        belayed(route(), ClimbMode.LEAD, belayer));
        var notAFriend =
                sessions.log(
                        stranger,
                        today().minusDays(2),
                        7,
                        belayed(route(), ClimbMode.LEAD, belayer));
        // Someone else's belay is none of mine.
        sessions.log(stranger, today(), 7, belayed(route(), ClimbMode.LEAD, friend));

        var first =
                mockMvc.perform(get("/api/v1/me/belays?size=2").with(as(belayer)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items", hasSize(2)))
                        .andExpect(jsonPath("$.items[0].sessionId").value(shared.id().toString()))
                        .andExpect(jsonPath("$.items[0].climber.nickname").value(friend.nickname()))
                        .andExpect(jsonPath("$.items[0].climbedOn").value(today().toString()))
                        .andExpect(jsonPath("$.items[0].routeCount").value(2))
                        .andExpect(jsonPath("$.items[0].readable").value(true))
                        .andExpect(jsonPath("$.items[1].sessionId").value(hidden.id().toString()))
                        .andExpect(jsonPath("$.items[1].readable").value(false))
                        .andExpect(jsonPath("$.nextCursor").isString())
                        .andReturn();
        String cursor = JsonPath.read(first.getResponse().getContentAsString(), "$.nextCursor");
        mockMvc.perform(get("/api/v1/me/belays?size=2&cursor=" + cursor).with(as(belayer)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].sessionId").value(notAFriend.id().toString()))
                .andExpect(jsonPath("$.items[0].routeCount").value(1))
                .andExpect(jsonPath("$.items[0].readable").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

        // Being named opens nothing.
        mockMvc.perform(get("/api/v1/sessions/" + hidden.id()).with(as(belayer)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/sessions/" + notAFriend.id()).with(as(belayer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void theBelayerTakesTheirNameBack() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var belayer = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        var session =
                sessions.log(
                        climber,
                        today(),
                        7,
                        belayed(route(), ClimbMode.LEAD, belayer),
                        belayed(route(), ClimbMode.LEAD, other),
                        belayed(route(), ClimbMode.TOP_ROPE, belayer));

        // Only the one named takes it back: not the climber, not a third party.
        for (var notNamed : new Member[] {climber, members.create(Role.ADMIN)}) {
            withdraw(notNamed, session).andExpect(status().isNotFound());
        }
        withdraw(belayer, session).andExpect(status().isNoContent());
        withdraw(belayer, session).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(climber)))
                .andExpect(jsonPath("$.ascents[0].belayer").doesNotExist())
                .andExpect(jsonPath("$.ascents[1].belayer.nickname").value(other.nickname()))
                .andExpect(jsonPath("$.ascents[2].belayer").doesNotExist());
        mockMvc.perform(get("/api/v1/me/belays").with(as(belayer)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void evenInAFrozenMonth() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var belayer = members.create(Role.CLIMBER);
        var old =
                sessions.log(
                        climber,
                        today().minusMonths(3),
                        7,
                        belayed(route(), ClimbMode.LEAD, belayer));

        withdraw(belayer, old).andExpect(status().isNoContent());
    }

    @Test
    void deletedAccountsLeaveNoBelayBehind() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var belayer = members.create(Role.CLIMBER);
        var leaving = members.create(Role.CLIMBER);
        var mine = sessions.log(climber, today(), 7, belayed(route(), ClimbMode.LEAD, leaving));
        sessions.log(leaving, today(), 7, belayed(route(), ClimbMode.LEAD, belayer));

        deleteAccount(leaving);

        mockMvc.perform(get("/api/v1/sessions/" + mine.id()).with(as(climber)))
                .andExpect(jsonPath("$.ascents[0].belayer").doesNotExist());
        // Their sessions went to "Ancien·ne membre", without my name on them.
        mockMvc.perform(get("/api/v1/me/belays").with(as(belayer)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void myExportSaysWhoBelayedMeAndWhomIBelayed() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var belayer = members.create(Role.CLIMBER);
        sessions.log(
                climber,
                today(),
                7,
                belayed(route(), ClimbMode.LEAD, belayer),
                belayed(route(), ClimbMode.LEAD, belayer));

        mockMvc.perform(get("/api/v1/me/export").with(as(climber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions[0].ascents[0].belayer").value(belayer.nickname()));
        mockMvc.perform(get("/api/v1/me/export").with(as(belayer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.belays[0].session.climber").value(climber.nickname()))
                .andExpect(jsonPath("$.belays[0].session.climbedOn").value(today().toString()))
                .andExpect(jsonPath("$.belays[0].routeCount").value(2));
    }

    private Route route() {
        return routes.create(members.create(Role.SETTER));
    }

    private ResultActions save(Member me, String... ascents) throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(UUID.randomUUID(), ascents)));
    }

    private String body(UUID clientRef, String... ascents) {
        return """
               {"clientRef": "%s", "climbedOn": "%s", "visibility": "FRIENDS", "ascents": [%s]}
               """
                .formatted(clientRef, today(), String.join(", ", ascents));
    }

    private static String ascent(Route route, UUID belayerId) {
        return """
               {"routeId": "%s", "mode": "LEAD", "topped": true, "clean": true, "falls": 0,
                "firstTry": false%s}
               """
                .formatted(
                        route.id(),
                        belayerId == null ? "" : ", \"belayerId\": \"" + belayerId + "\"");
    }

    private ResultActions withdraw(Member member, ClimbingSession session) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/sessions/" + session.id() + "/belayer")
                        .with(as(member))
                        .with(xsrf()));
    }

    private void deleteAccount(Member member) throws Exception {
        var cookie = MemberFixtures.signIn(mockMvc, member, true);
        mockMvc.perform(
                        post("/api/v1/me/deletion")
                                .cookie(cookie)
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"password\": \"%s\"}"
                                                .formatted(MemberFixtures.PASSWORD)))
                .andExpect(status().isNoContent());
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }
}
