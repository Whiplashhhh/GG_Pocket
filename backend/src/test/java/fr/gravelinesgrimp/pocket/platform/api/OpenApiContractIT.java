package fr.gravelinesgrimp.pocket.platform.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.RepositoryFiles;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The frontend generates its API types from the committed contract. This test fails as soon as the
 * code and the committed contract diverge, and always writes the real contract to {@code
 * target/openapi.json}. To update the committed one: {@code ./mvnw verify -Dgg.openapi.write=true},
 * then {@code pnpm api:types} in {@code frontend/}.
 */
@IntegrationTest
class OpenApiContractIT {

    private static final Path GENERATED = Path.of("target", "openapi.json");
    private static final JsonMapper JSON =
            JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    @Autowired private MockMvc mockMvc;

    @Test
    void committedContractMatchesTheCode() throws Exception {
        var body =
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        var actual = pretty(body);
        Files.createDirectories(GENERATED.getParent());
        Files.writeString(GENERATED, actual);

        if (Boolean.getBoolean("gg.openapi.write")) {
            Files.writeString(RepositoryFiles.OPENAPI_CONTRACT, actual);
            return;
        }

        assertThat(RepositoryFiles.OPENAPI_CONTRACT).exists();
        var committed = pretty(Files.readString(RepositoryFiles.OPENAPI_CONTRACT));
        assertThat(actual)
                .as(
                        "The API changed but frontend/src/api/openapi.json was not updated. Run"
                                + " ./mvnw verify -Dgg.openapi.write=true (or copy"
                                + " backend/target/openapi.json), then pnpm api:types in"
                                + " frontend/.")
                .isEqualTo(committed);
    }

    private static String pretty(String json) {
        return JSON.writeValueAsString(JSON.readTree(json)) + "\n";
    }
}
