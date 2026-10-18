package fr.gravelinesgrimp.pocket.platform.api.meta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.RepositoryFiles;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class MetaControllerIT {

    @Autowired private MockMvc mockMvc;

    @Test
    void anonymousVisitorsCanReadWhatIsRunning() throws Exception {
        mockMvc.perform(get("/api/v1/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(RepositoryFiles.version()))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.commit").value("unknown"))
                .andExpect(jsonPath("$.branch").value("unknown"));
    }
}
