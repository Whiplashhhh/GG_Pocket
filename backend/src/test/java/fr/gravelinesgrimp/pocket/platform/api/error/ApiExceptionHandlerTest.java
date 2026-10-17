package fr.gravelinesgrimp.pocket.platform.api.error;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.testsupport.ErrorProbeController;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Mapping of each kind of failure to a Problem Details response, without the security chain. */
@WebMvcTest(controllers = ErrorProbeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ErrorProbeController.class)
class ApiExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void invalidBodyListsEveryInvalidField() throws Exception {
        mockMvc.perform(
                        post("/probe/validated")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"\", \"count\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "count")))
                .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder("NotBlank", "Min")));
    }

    @Test
    void unreadableBodyIsAMalformedRequest() throws Exception {
        mockMvc.perform(
                        post("/probe/validated")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void wrongContentTypeIsReported() throws Exception {
        mockMvc.perform(post("/probe/validated").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void businessExceptionKeepsItsCodeAndMessage() throws Exception {
        mockMvc.perform(get("/probe/business"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.detail").value("Only the president can do that."));
    }

    @Test
    void accessDeniedForAnAnonymousVisitorAsksToLogIn() throws Exception {
        mockMvc.perform(get("/probe/denied"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @WithMockUser
    void accessDeniedForAMemberIsForbidden() throws Exception {
        mockMvc.perform(get("/probe/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void unexpectedErrorNeverLeaksInternalDetails() throws Exception {
        mockMvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(Matchers.containsString("hunter2"))));
    }
}
