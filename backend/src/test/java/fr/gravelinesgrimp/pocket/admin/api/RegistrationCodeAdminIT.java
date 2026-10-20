package fr.gravelinesgrimp.pocket.admin.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.freshAddress;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

@IntegrationTest
class RegistrationCodeAdminIT {

    private static final String OPEN_DAY_ACTIVE =
            "{\"label\": \"Portes ouvertes\", \"active\": true}";
    private static final String OPEN_DAY_INACTIVE =
            "{\"label\": \"Portes ouvertes\", \"active\": false}";

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;

    @Test
    void theAdminCreatesACodeThatOpensRegistration() throws Exception {
        var admin = members.create(Role.ADMIN);
        var created =
                mockMvc.perform(
                                post("/api/v1/admin/registration-codes")
                                        .with(as(admin))
                                        .with(xsrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"label": "Affiche saison 2026", "maxUses": 30,
                                                 "active": true}
                                                """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.code", matchesRegex("[A-Z2-9]{4}-[A-Z2-9]{4}")))
                        .andExpect(jsonPath("$.useCount").value(0))
                        .andExpect(jsonPath("$.state").value("USABLE"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String code = JsonPath.read(created, "$.code");

        mockMvc.perform(
                        post("/api/v1/auth/registration-code/check")
                                .with(xsrf())
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\": \"%s\"}".formatted(code)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/admin/registration-codes").with(as(admin)))
                .andExpect(jsonPath("$[*].code", hasItem(code)));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(admin)))
                .andExpect(jsonPath("$.entries[0].action").value("REGISTRATION_CODE_CREATED"));
    }

    @Test
    void aDeactivatedCodeNoLongerWorks() throws Exception {
        var admin = members.create(Role.ADMIN);
        var created =
                mockMvc.perform(
                                post("/api/v1/admin/registration-codes")
                                        .with(as(admin))
                                        .with(xsrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"label\": \"Portes ouvertes\", \"active\":"
                                                        + " true}"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String id = JsonPath.read(created, "$.id");
        String code = JsonPath.read(created, "$.code");

        mockMvc.perform(
                        put("/api/v1/admin/registration-codes/" + id)
                                .with(as(admin))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OPEN_DAY_INACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("INACTIVE"));

        mockMvc.perform(
                        post("/api/v1/auth/registration-code/check")
                                .with(xsrf())
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\": \"%s\"}".formatted(code)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theFirstStartCodeIsNeverListed() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/registration-codes")
                                .with(as(members.create(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].label", not(hasItem("Premier démarrage"))));
    }

    @Test
    void theBoardDoesNotManageCodes() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/registration-codes")
                                .with(as(members.create(Role.BOARD))))
                .andExpect(status().isForbidden());
    }
}
