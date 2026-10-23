package fr.gravelinesgrimp.pocket.club.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** The club layout: read by everyone, changed by the admin only. */
@IntegrationTest
class ClubAdminIT {

    /** Cords 1 to 29 are seeded; this class uses 40 to 79 (RouteIT uses 80 and above). */
    private static final AtomicInteger CORD_NUMBER = new AtomicInteger(40);

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;

    @Test
    void everyMemberReadsTheRealClubLayout() throws Exception {
        mockMvc.perform(get("/api/v1/topo/config").with(as(members.create(Role.CLIMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grades", hasSize(36)))
                .andExpect(jsonPath("$.grades[0].code").value("4a-"))
                .andExpect(jsonPath("$.grades[35].code").value("7c+"))
                .andExpect(jsonPath("$.grades[35].rank").value(36))
                .andExpect(
                        jsonPath("$.sectors[?(@.name == 'Neutre 8 m')].cords[*].number")
                                .value(hasItems(1, 2, 3)))
                .andExpect(
                        jsonPath("$.holdColors[?(@.label == 'Multicolore')].multicolor")
                                .value(contains(true)))
                .andExpect(
                        jsonPath("$.holdColors[?(@.label == 'Jaune')].hex")
                                .value(contains("#F5C518")));
    }

    @Test
    void theAdminCreatesAndRenamesASector() throws Exception {
        var admin = members.create(Role.ADMIN);
        var name = uniqueName("Bloc");
        var created =
                sector(admin, post("/api/v1/admin/club/sectors"), name, "Le coin des costauds")
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value(name))
                        .andExpect(jsonPath("$.cords", hasSize(0)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(created, "$.id");

        var renamed = uniqueName("Bloc bis");
        sector(admin, put("/api/v1/admin/club/sectors/" + id), renamed, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(renamed))
                .andExpect(jsonPath("$.description").doesNotExist());
        mockMvc.perform(get("/api/v1/admin/audit").with(as(admin)))
                .andExpect(jsonPath("$.entries[0].action").value("CLUB_SECTOR_UPDATED"))
                .andExpect(jsonPath("$.entries[0].targetType").value("SECTOR"));
    }

    @Test
    void sectorNamesAreUniqueWhateverTheCase() throws Exception {
        var admin = members.create(Role.ADMIN);
        sector(admin, post("/api/v1/admin/club/sectors"), "grand TOIT", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
    }

    @Test
    void theAdminAddsAndMovesACord() throws Exception {
        var admin = members.create(Role.ADMIN);
        var sectors = sectorIds(admin);
        var number = CORD_NUMBER.getAndIncrement();
        var created =
                cord(admin, post("/api/v1/admin/club/cords"), number, sectors.get(0), true)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.number").value(number))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(created, "$.id");

        cord(admin, put("/api/v1/admin/club/cords/" + id), number, sectors.get(1), false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectorId").value(sectors.get(1)))
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(admin)))
                .andExpect(jsonPath("$.entries[0].action").value("CLUB_CORD_UPDATED"));
    }

    @Test
    void cordNumbersAreUniqueAndBetween1And99() throws Exception {
        var admin = members.create(Role.ADMIN);
        var sector = sectorIds(admin).get(0);
        cord(admin, post("/api/v1/admin/club/cords"), 12, sector, true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
        cord(admin, post("/api/v1/admin/club/cords"), 100, sector, true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void theAdminAddsAndDeactivatesAHoldColour() throws Exception {
        var admin = members.create(Role.ADMIN);
        var label = uniqueName("Fluo");
        var created =
                color(admin, post("/api/v1/admin/club/hold-colors"), label, "#ccff00", true)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.hex").value("#CCFF00"))
                        .andExpect(jsonPath("$.multicolor").value(false))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(created, "$.id");

        color(admin, put("/api/v1/admin/club/hold-colors/" + id), label, "#CCFF00", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        color(admin, post("/api/v1/admin/club/hold-colors"), "JAUNE", "#FFFF00", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
        color(admin, post("/api/v1/admin/club/hold-colors"), uniqueName("Bad"), "red", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void theAdminReordersTheSectors() throws Exception {
        var admin = members.create(Role.ADMIN);
        var reversed = new ArrayList<>(sectorIds(admin));
        Collections.reverse(reversed);
        try {
            reorder(admin, reversed)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sectors[0].id").value(reversed.get(0)))
                    .andExpect(jsonPath("$.sectors[0].position").value(1));
            reorder(admin, reversed.subList(1, reversed.size()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        } finally {
            Collections.reverse(reversed);
            reorder(admin, reversed).andExpect(status().isOk());
        }
    }

    @Test
    void onlyTheAdminChangesTheLayout() throws Exception {
        var board = members.create(Role.BOARD);
        sector(board, post("/api/v1/admin/club/sectors"), uniqueName("Interdit"), null)
                .andExpect(status().isForbidden());
        color(board, post("/api/v1/admin/club/hold-colors"), uniqueName("Nope"), "#123456", true)
                .andExpect(status().isForbidden());
        var admin = members.create(Role.ADMIN);
        cord(
                        members.create(Role.SETTER),
                        post("/api/v1/admin/club/cords"),
                        CORD_NUMBER.getAndIncrement(),
                        sectorIds(admin).get(0),
                        true)
                .andExpect(status().isForbidden());
    }

    private List<String> sectorIds(Member admin) throws Exception {
        var config =
                mockMvc.perform(get("/api/v1/topo/config").with(as(admin)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(config, "$.sectors[*].id");
    }

    private ResultActions sector(
            Member actor, MockHttpServletRequestBuilder request, String name, String description)
            throws Exception {
        return mockMvc.perform(
                request.with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                description == null
                                        ? "{\"name\": \"%s\"}".formatted(name)
                                        : "{\"name\": \"%s\", \"description\": \"%s\"}"
                                                .formatted(name, description)));
    }

    private ResultActions cord(
            Member actor,
            MockHttpServletRequestBuilder request,
            int number,
            String sectorId,
            boolean active)
            throws Exception {
        return mockMvc.perform(
                request.with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"number\": %d, \"sectorId\": \"%s\", \"active\": %s}"
                                        .formatted(number, sectorId, active)));
    }

    private ResultActions color(
            Member actor,
            MockHttpServletRequestBuilder request,
            String label,
            String hex,
            boolean active)
            throws Exception {
        return mockMvc.perform(
                request.with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"label\": \"%s\", \"hex\": \"%s\", \"multicolor\": false,"
                                                .formatted(label, hex)
                                        + " \"active\": %s}".formatted(active)));
    }

    private ResultActions reorder(Member admin, List<String> sectorIds) throws Exception {
        var ids = sectorIds.stream().map(id -> "\"" + id + "\"").toList();
        return mockMvc.perform(
                put("/api/v1/admin/club/sector-order")
                        .with(as(admin))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectorIds\": [" + String.join(", ", ids) + "]}"));
    }

    private static String uniqueName(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
    }
}
