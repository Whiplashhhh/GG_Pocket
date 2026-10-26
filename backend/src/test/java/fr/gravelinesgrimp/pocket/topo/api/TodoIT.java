package fr.gravelinesgrimp.pocket.topo.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;
import static fr.gravelinesgrimp.pocket.SessionFixtures.topped;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;

/** "À faire": my marks and clean ascents, per mode, on the routes on the wall only. */
@IntegrationTest
class TodoIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void eachRouteOnTheWallSaysWhereIStandInEachMode() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var untouched = routes.create(setter);
        var byHand = routes.create(setter);
        var inLead = routes.create(setter);
        var cleanInTopRope = routes.create(setter, RouteStatus.LAST_CHANCE);
        var toppedOnly = routes.create(setter);
        var dismounted = routes.create(setter, RouteStatus.DISMOUNTED);

        check(me, byHand, "TOP_ROPE").andExpect(status().isNoContent());
        check(me, inLead, "LEAD").andExpect(status().isNoContent());
        sessions.log(
                me,
                ClubTime.today(clock),
                null,
                clean(cleanInTopRope, ClimbMode.TOP_ROPE),
                topped(toppedOnly, ClimbMode.LEAD));

        todo(me).andExpect(status().isOk())
                .andExpect(jsonPath("$.routeCount").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath(state(untouched, "topRope")).value(contains("TODO")))
                .andExpect(jsonPath(state(untouched, "lead")).value(contains("TODO")))
                .andExpect(jsonPath(state(byHand, "topRope")).value(contains("CHECKED")))
                .andExpect(jsonPath(state(byHand, "lead")).value(contains("TODO")))
                .andExpect(jsonPath(state(inLead, "topRope")).value(contains("FROM_LEAD")))
                .andExpect(jsonPath(state(inLead, "lead")).value(contains("CHECKED")))
                .andExpect(
                        jsonPath(state(cleanInTopRope, "topRope")).value(contains("FROM_ASCENT")))
                .andExpect(jsonPath(state(cleanInTopRope, "lead")).value(contains("TODO")))
                .andExpect(jsonPath(state(toppedOnly, "lead")).value(contains("TODO")))
                .andExpect(
                        jsonPath("$.routes[*].route.id", not(hasItem(dismounted.id().toString()))));
    }

    @Test
    void theCountersAddUpMyKnownRoutes() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var first = routes.create(setter);
        var second = routes.create(setter);

        todo(me).andExpect(jsonPath("$.topRopeKnown").value(0))
                .andExpect(jsonPath("$.leadKnown").value(0));
        check(me, first, "LEAD").andExpect(status().isNoContent());
        check(me, second, "TOP_ROPE").andExpect(status().isNoContent());
        todo(me).andExpect(jsonPath("$.topRopeKnown").value(2))
                .andExpect(jsonPath("$.leadKnown").value(1));

        // Undo: unticking by hand puts the route back on the list.
        uncheck(me, second, "TOP_ROPE").andExpect(status().isNoContent());
        todo(me).andExpect(jsonPath("$.topRopeKnown").value(1))
                .andExpect(jsonPath(state(second, "topRope")).value(contains("TODO")));
    }

    @Test
    void iNeverSeeTheMarksOrAscentsOfSomeoneElse() throws Exception {
        var me = members.create(Role.CLIMBER);
        var someoneElse = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        check(someoneElse, route, "LEAD").andExpect(status().isNoContent());
        sessions.log(someoneElse, ClubTime.today(clock), 7, clean(route, ClimbMode.TOP_ROPE));

        todo(me).andExpect(jsonPath("$.topRopeKnown").value(0))
                .andExpect(jsonPath("$.leadKnown").value(0))
                .andExpect(jsonPath(state(route, "topRope")).value(contains("TODO")))
                .andExpect(jsonPath(state(route, "lead")).value(contains("TODO")))
                .andExpect(jsonPath("$.routes[*].route.checks[*]").value(empty()));
    }

    private static String state(Route route, String mode) {
        return "$.routes[?(@.route.id == '" + route.id() + "')]." + mode;
    }

    private ResultActions todo(Member me) throws Exception {
        return mockMvc.perform(get("/api/v1/me/todo").with(as(me)));
    }

    private ResultActions check(Member me, Route route, String mode) throws Exception {
        return mockMvc.perform(
                put("/api/v1/routes/" + route.id() + "/checks/" + mode).with(as(me)).with(xsrf()));
    }

    private ResultActions uncheck(Member me, Route route, String mode) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/routes/" + route.id() + "/checks/" + mode)
                        .with(as(me))
                        .with(xsrf()));
    }
}
