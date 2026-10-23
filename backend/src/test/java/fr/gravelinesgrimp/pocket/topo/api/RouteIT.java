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

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.club.domain.Cord;
import fr.gravelinesgrimp.pocket.club.domain.HoldColor;
import fr.gravelinesgrimp.pocket.club.infra.CordRepository;
import fr.gravelinesgrimp.pocket.club.infra.HoldColorRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteDraft;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Routes: opened and managed by setters and above, read by everyone. */
@IntegrationTest
class RouteIT {

    /** Cords 1 to 29 are seeded, ClubAdminIT uses 40 to 79. */
    private static final AtomicInteger CORD_NUMBER = new AtomicInteger(80);

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private RouteRepository routeRepository;
    @Autowired private CordRepository cords;
    @Autowired private HoldColorRepository holdColors;
    @Autowired private Clock clock;

    @Test
    void aSetterOpensARouteThatEveryoneSees() throws Exception {
        var setter = members.create(Role.SETTER);
        var draft = draft(setter, List.of(color("Bleu"), color("Jaune")), today());
        var created =
                save(setter, post("/api/v1/routes"), draft)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.route.name").value(draft.name()))
                        .andExpect(jsonPath("$.route.grade").value("6a"))
                        .andExpect(jsonPath("$.route.gradeRank").value(20))
                        .andExpect(jsonPath("$.route.cordNumber").value(1))
                        .andExpect(jsonPath("$.route.sectorName").value("Neutre 8 m"))
                        .andExpect(
                                jsonPath("$.route.holds[*].label").value(contains("Bleu", "Jaune")))
                        .andExpect(jsonPath("$.route.status").value("ACTIVE"))
                        .andExpect(jsonPath("$.route.styles").value(contains("SLAB", "CRIMPS")))
                        .andExpect(jsonPath("$.route.favoriteCount").value(0))
                        .andExpect(jsonPath("$.route.favorite").value(false))
                        .andExpect(jsonPath("$.route.photoUrl").doesNotExist())
                        .andExpect(jsonPath("$.setters[0].nickname").value(setter.nickname()))
                        .andExpect(jsonPath("$.advice").value(draft.advice()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(created, "$.route.id");

        var climber = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/routes").with(as(climber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mockMvc.perform(get("/api/v1/routes/" + id).with(as(climber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.id").value(id))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void aClimberCannotOpenOrManageRoutes() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        save(climber, post("/api/v1/routes"), routes.draft(members.create(Role.SETTER)))
                .andExpect(status().isForbidden());
        save(climber, put("/api/v1/routes/" + route.id()), routes.draft(climber))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        post("/api/v1/routes/" + route.id() + "/dismount")
                                .with(as(climber))
                                .with(xsrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/routes/setters").with(as(climber)))
                .andExpect(status().isForbidden());
    }

    @Test
    void theSetterListHasActiveSettersOnly() throws Exception {
        var setter = members.create(Role.SETTER);
        var climber = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/routes/setters").with(as(setter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nickname", hasItem(setter.nickname())))
                .andExpect(jsonPath("$[*].nickname", not(hasItem(climber.nickname()))));
    }

    @Test
    void aRouteNeedsValidSettersAndAPastOpeningDate() throws Exception {
        var setter = members.create(Role.SETTER);
        var yellow = List.of(color("Jaune"));
        save(setter, post("/api/v1/routes"), draft(setter, yellow, today().plusDays(1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATE_IN_THE_FUTURE"));
        var climber = members.create(Role.CLIMBER);
        save(setter, post("/api/v1/routes"), withSetters(setter, List.of(climber.id()), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SETTERS"));
        save(setter, post("/api/v1/routes"), withSetters(setter, List.of(), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SETTERS"));
        save(
                        setter,
                        post("/api/v1/routes"),
                        withSetters(setter, List.of(), "Un ouvreur de passage"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setterNote").value("Un ouvreur de passage"));
        var red = color("Rouge");
        save(setter, post("/api/v1/routes"), draft(setter, List.of(red, red), today()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void inactiveCordsAndColoursTakeNoNewRoute() throws Exception {
        var setter = members.create(Role.SETTER);
        var sectorId = cords.findById(routes.cordId(1)).orElseThrow().sectorId();
        var cord = cords.saveAndFlush(new Cord(CORD_NUMBER.getAndIncrement(), sectorId, false));
        var color =
                holdColors.saveAndFlush(
                        new HoldColor(
                                "Éteinte " + UUID.randomUUID().toString().substring(0, 8),
                                "#101010",
                                false,
                                false,
                                99));
        save(setter, post("/api/v1/routes"), onCord(setter, cord.id(), color("Jaune")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CLUB_ITEM_INACTIVE"));
        save(setter, post("/api/v1/routes"), onCord(setter, routes.cordId(1), color.id()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CLUB_ITEM_INACTIVE"));
    }

    @Test
    void aRouteKeepsItsCordWhenTheCordIsDeactivated() throws Exception {
        var setter = members.create(Role.SETTER);
        var sectorId = cords.findById(routes.cordId(1)).orElseThrow().sectorId();
        var cord = cords.saveAndFlush(new Cord(CORD_NUMBER.getAndIncrement(), sectorId, true));
        var draft = onCord(setter, cord.id(), color("Vert"));
        var route = routeRepository.saveAndFlush(new Route(draft, setter.id()));
        cord.update(cord.number(), sectorId, false);
        cords.saveAndFlush(cord);

        save(setter, put("/api/v1/routes/" + route.id()), draft)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cordId").value(cord.id().toString()));
    }

    @Test
    void theLastChanceWarnsEveryoneOnceUntilItIsCancelled() throws Exception {
        var setter = members.create(Role.SETTER);
        var climber = members.create(Role.CLIMBER);
        var id = routes.create(setter).id().toString();
        var planned = today().plusDays(10).toString();

        lastChance(setter, id, "{\"plannedDismountOn\": \"" + planned + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.status").value("LAST_CHANCE"))
                .andExpect(jsonPath("$.route.plannedDismountOn").value(planned));
        mockMvc.perform(get("/api/v1/routes/last-chance/unacknowledged").with(as(climber)))
                .andExpect(jsonPath("$[*].id", hasItem(id)));

        acknowledge(climber, id).andExpect(status().isNoContent());
        acknowledge(climber, id).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/routes/last-chance/unacknowledged").with(as(climber)))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/routes/last-chance").with(as(setter)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.route.id == '" + id + "')].acknowledgedCount")
                                .value(contains(1)));

        // Leaving the last chance forgets who saw it: announced again, it warns again.
        mockMvc.perform(
                        delete("/api/v1/routes/" + id + "/last-chance")
                                .with(as(setter))
                                .with(xsrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.status").value("ACTIVE"))
                .andExpect(jsonPath("$.route.plannedDismountOn").doesNotExist());
        lastChance(setter, id, "{}").andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/routes/last-chance/unacknowledged").with(as(climber)))
                .andExpect(jsonPath("$[*].id", hasItem(id)));
    }

    @Test
    void theDismountCannotBePlannedInThePast() throws Exception {
        var setter = members.create(Role.SETTER);
        var id = routes.create(setter).id().toString();
        lastChance(setter, id, "{\"plannedDismountOn\": \"" + today().minusDays(1) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATE_IN_THE_PAST"));
    }

    @Test
    void aDismountedRouteGoesToTheArchiveReadOnlyUntilRemounted() throws Exception {
        var setter = members.create(Role.SETTER);
        var climber = members.create(Role.CLIMBER);
        var route = routes.create(setter, RouteStatus.LAST_CHANCE);
        var id = route.id().toString();

        changeStatus(setter, id, "dismount")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.status").value("DISMOUNTED"))
                .andExpect(jsonPath("$.route.dismountedAt").exists());
        mockMvc.perform(get("/api/v1/routes").with(as(climber)))
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mockMvc.perform(get("/api/v1/routes/archive").with(as(climber)))
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mockMvc.perform(get("/api/v1/routes/" + id).with(as(climber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.status").value("DISMOUNTED"));

        save(setter, put("/api/v1/routes/" + id), routes.draft(setter))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
        lastChance(setter, id, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_DISMOUNTED"));
        changeStatus(setter, id, "dismount")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_STATUS_CONFLICT"));

        changeStatus(setter, id, "remount")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.status").value("ACTIVE"))
                .andExpect(jsonPath("$.route.dismountedAt").doesNotExist());
        changeStatus(setter, id, "remount")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_STATUS_CONFLICT"));
        mockMvc.perform(
                        delete("/api/v1/routes/" + id + "/last-chance")
                                .with(as(setter))
                                .with(xsrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROUTE_STATUS_CONFLICT"));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(members.create(Role.BOARD))))
                .andExpect(jsonPath("$.entries[0].action").value("ROUTE_REMOUNTED"))
                .andExpect(jsonPath("$.entries[0].targetLabel").value(route.name()))
                .andExpect(jsonPath("$.entries[1].action").value("ROUTE_DISMOUNTED"))
                .andExpect(jsonPath("$.entries[1].before").value("LAST_CHANCE"));
    }

    @Test
    void anUnknownRouteIsNotFound() throws Exception {
        mockMvc.perform(
                        get("/api/v1/routes/" + UUID.randomUUID())
                                .with(as(members.create(Role.CLIMBER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }

    private UUID color(String label) {
        return routes.holdColorId(label);
    }

    private RouteDraft draft(Member setter, List<UUID> colors, LocalDate openedOn) {
        var base = routes.draft(setter);
        return new RouteDraft(
                base.name(),
                base.gradeId(),
                base.cordId(),
                colors,
                openedOn,
                base.setterIds(),
                null,
                base.advice(),
                base.styles());
    }

    private RouteDraft withSetters(Member author, List<UUID> setterIds, String setterNote) {
        var base = routes.draft(author);
        return new RouteDraft(
                base.name(),
                base.gradeId(),
                base.cordId(),
                base.holdColorIds(),
                base.openedOn(),
                setterIds,
                setterNote,
                null,
                base.styles());
    }

    private RouteDraft onCord(Member setter, UUID cordId, UUID colorId) {
        var base = routes.draft(setter);
        return new RouteDraft(
                base.name(),
                base.gradeId(),
                cordId,
                List.of(colorId),
                base.openedOn(),
                base.setterIds(),
                null,
                null,
                base.styles());
    }

    private ResultActions save(
            Member actor, MockHttpServletRequestBuilder request, RouteDraft draft)
            throws Exception {
        return mockMvc.perform(
                request.with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(draft)));
    }

    private ResultActions lastChance(Member actor, String routeId, String body) throws Exception {
        return mockMvc.perform(
                put("/api/v1/routes/" + routeId + "/last-chance")
                        .with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    private ResultActions acknowledge(Member actor, String routeId) throws Exception {
        return mockMvc.perform(
                post("/api/v1/routes/last-chance/acknowledgements")
                        .with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"routeIds\": [\"" + routeId + "\"]}"));
    }

    private ResultActions changeStatus(Member actor, String routeId, String action)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/routes/" + routeId + "/" + action).with(as(actor)).with(xsrf()));
    }

    /** The JSON the app sends for a route form. */
    static String json(RouteDraft draft) {
        return """
                {"name": "%s", "gradeId": "%s", "cordId": "%s", "holdColorIds": [%s],
                 "openedOn": "%s", "setterIds": [%s], %s %s "styles": [%s]}
                """
                .formatted(
                        draft.name(),
                        draft.gradeId(),
                        draft.cordId(),
                        quoted(draft.holdColorIds()),
                        draft.openedOn(),
                        quoted(draft.setterIds()),
                        draft.setterNote() == null
                                ? ""
                                : "\"setterNote\": \"" + draft.setterNote() + "\",",
                        draft.advice() == null ? "" : "\"advice\": \"" + draft.advice() + "\",",
                        quoted(draft.styles()));
    }

    private static String quoted(List<?> values) {
        return values.stream().map(value -> "\"" + value + "\"").collect(Collectors.joining(", "));
    }
}
