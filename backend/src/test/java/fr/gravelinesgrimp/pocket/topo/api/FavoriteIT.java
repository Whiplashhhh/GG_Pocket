package fr.gravelinesgrimp.pocket.topo.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import java.util.UUID;

/** Hearts, "je sais la faire" and the favourite route: each member only touches their own. */
@IntegrationTest
class FavoriteIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;

    @Test
    void aFullHeartRefusesAThirdRouteUnlessOneIsGivenUp() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var first = routes.create(setter);
        var second = routes.create(setter);
        var third = routes.create(setter);

        heart(me, first, null).andExpect(status().isNoContent());
        heart(me, second, null).andExpect(status().isNoContent());
        heart(me, second, null).andExpect(status().isNoContent());
        heart(me, third, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITES_FULL"));
        heart(me, third, UUID.randomUUID())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITES_FULL"));

        heart(me, third, first.id()).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me/favorites").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].id")
                                .value(contains(third.id().toString(), second.id().toString())));
        mockMvc.perform(get("/api/v1/routes/" + third.id()).with(as(me)))
                .andExpect(jsonPath("$.route.favorite").value(true))
                .andExpect(jsonPath("$.route.favoriteCount").value(1));
        mockMvc.perform(get("/api/v1/routes/" + third.id()).with(as(setter)))
                .andExpect(jsonPath("$.route.favorite").value(false))
                .andExpect(jsonPath("$.route.favoriteCount").value(1));
    }

    @Test
    void aDismountedRouteFreesItsPlaceButStaysInMyHistory() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var first = routes.create(setter);
        var second = routes.create(setter);
        var third = routes.create(setter);
        heart(me, first, null).andExpect(status().isNoContent());
        heart(me, second, null).andExpect(status().isNoContent());

        mockMvc.perform(
                        post("/api/v1/routes/" + first.id() + "/dismount")
                                .with(as(setter))
                                .with(xsrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.favoriteCount").value(1));

        heart(me, third, null).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me/favorites").with(as(me)))
                .andExpect(jsonPath("$[*].id", hasItem(first.id().toString())))
                .andExpect(
                        jsonPath("$[?(@.id == '" + first.id() + "')].status")
                                .value(contains("DISMOUNTED")));
        mockMvc.perform(
                        delete("/api/v1/routes/" + first.id() + "/favorite")
                                .with(as(me))
                                .with(xsrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
        heart(me, routes.create(setter, RouteStatus.DISMOUNTED), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
    }

    @Test
    void iTakeMyHeartBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        heart(me, route, null).andExpect(status().isNoContent());
        for (var i = 0; i < 2; i++) {
            mockMvc.perform(
                            delete("/api/v1/routes/" + route.id() + "/favorite")
                                    .with(as(me))
                                    .with(xsrf()))
                    .andExpect(status().isNoContent());
        }
        mockMvc.perform(get("/api/v1/me/favorites").with(as(me)))
                .andExpect(jsonPath("$[*].id", not(hasItem(route.id().toString()))));
    }

    @Test
    void iMarkTheModesInWhichICanClimbARoute() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));

        check(me, route, "LEAD", true).andExpect(status().isNoContent());
        check(me, route, "LEAD", true).andExpect(status().isNoContent());
        check(me, route, "TOP_ROPE", true).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/routes/" + route.id()).with(as(me)))
                .andExpect(jsonPath("$.route.checks").value(contains("TOP_ROPE", "LEAD")));
        mockMvc.perform(get("/api/v1/routes/" + route.id()).with(as(members.create(Role.CLIMBER))))
                .andExpect(jsonPath("$.route.checks").isEmpty());

        check(me, route, "LEAD", false).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/routes").with(as(me)))
                .andExpect(
                        jsonPath("$[?(@.id == '" + route.id() + "')].checks[*]")
                                .value(contains("TOP_ROPE")));
        check(me, route, "BOULDER", true).andExpect(status().isBadRequest());
        check(me, routes.create(members.create(Role.SETTER), RouteStatus.DISMOUNTED), "LEAD", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
    }

    @Test
    void myFavouriteRouteMayBeDismounted() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER), RouteStatus.DISMOUNTED);

        favoriteRoute(me, route.id()).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(jsonPath("$.favoriteRouteId").value(route.id().toString()));

        mockMvc.perform(delete("/api/v1/me/favorite-route").with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(jsonPath("$.favoriteRouteId").doesNotExist());
        favoriteRoute(me, UUID.randomUUID())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private ResultActions heart(Member me, Route route, UUID replaceRouteId) throws Exception {
        return mockMvc.perform(
                put("/api/v1/routes/" + route.id() + "/favorite")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                replaceRouteId == null
                                        ? "{}"
                                        : "{\"replaceRouteId\": \"" + replaceRouteId + "\"}"));
    }

    private ResultActions check(Member me, Route route, String mode, boolean on) throws Exception {
        var path = "/api/v1/routes/" + route.id() + "/checks/" + mode;
        return mockMvc.perform((on ? put(path) : delete(path)).with(as(me)).with(xsrf()));
    }

    private ResultActions favoriteRoute(Member me, UUID routeId) throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/favorite-route")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"routeId\": \"" + routeId + "\"}"));
    }
}
