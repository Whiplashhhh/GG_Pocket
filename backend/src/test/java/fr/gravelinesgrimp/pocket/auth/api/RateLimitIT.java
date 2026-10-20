package fr.gravelinesgrimp.pocket.auth.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Spec §6: sign-in, registration and reset attempts are limited. */
@IntegrationTest
class RateLimitIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;

    @Test
    void guessingRegistrationCodesIsCutShort() throws Exception {
        var address = "192.0.2.10";
        for (var i = 0; i < 20; i++) {
            mockMvc.perform(codeCheck(address)).andExpect(status().isBadRequest());
        }
        mockMvc.perform(codeCheck(address))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void tooManyWrongPasswordsLockTheNicknameForAWhileEvenFromElsewhere() throws Exception {
        var member = members.create(Role.CLIMBER);
        for (var i = 0; i < 10; i++) {
            mockMvc.perform(login(member.nickname(), "mauvais mot de passe"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(login(member.nickname(), MemberFixtures.PASSWORD))
                .andExpect(status().isTooManyRequests());
    }

    private static MockHttpServletRequestBuilder codeCheck(String address) {
        return post("/api/v1/auth/registration-code/check")
                .with(xsrf())
                .with(
                        request -> {
                            request.setRemoteAddr(address);
                            return request;
                        })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\": \"ZZZZ-ZZZZ\"}");
    }

    private static MockHttpServletRequestBuilder login(String nickname, String password) {
        return post("/api/v1/auth/login")
                .with(xsrf())
                .with(MemberFixtures.freshAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"nickname\": \"%s\", \"password\": \"%s\", \"rememberMe\": true}"
                                .formatted(nickname, password));
    }
}
