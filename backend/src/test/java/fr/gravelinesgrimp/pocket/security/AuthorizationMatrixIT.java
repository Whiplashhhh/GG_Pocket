package fr.gravelinesgrimp.pocket.security;

import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Definition of Done: every endpoint, every role, allowed AND refused. Anonymous callers get 401,
 * members below the required role get 403; allowed callers get anything else (2xx, or 4xx from the
 * request itself, such as an unknown id).
 */
@IntegrationTest
class AuthorizationMatrixIT {

    /** {@code null} minimum role: public. */
    record Endpoint(HttpMethod method, String path, String body, Role minimumRole) {

        @Override
        public String toString() {
            return method + " " + path;
        }
    }

    private static final String SOME_ID = UUID.randomUUID().toString();

    private static final List<Endpoint> ENDPOINTS =
            List.of(
                    new Endpoint(HttpMethod.GET, "/api/v1/me", null, Role.CLIMBER),
                    new Endpoint(HttpMethod.PUT, "/api/v1/me/profile", "{}", Role.CLIMBER),
                    new Endpoint(HttpMethod.PUT, "/api/v1/me/avatar", "{}", Role.CLIMBER),
                    new Endpoint(HttpMethod.DELETE, "/api/v1/me/avatar", null, Role.CLIMBER),
                    new Endpoint(HttpMethod.PUT, "/api/v1/me/password", "{}", Role.CLIMBER),
                    new Endpoint(HttpMethod.GET, "/api/v1/me/devices", null, Role.CLIMBER),
                    new Endpoint(
                            HttpMethod.DELETE, "/api/v1/me/devices/" + SOME_ID, null, Role.CLIMBER),
                    new Endpoint(
                            HttpMethod.POST, "/api/v1/me/devices/close-others", null, Role.CLIMBER),
                    new Endpoint(HttpMethod.GET, "/api/v1/media/" + SOME_ID, null, Role.CLIMBER),
                    new Endpoint(HttpMethod.GET, "/api/v1/admin/members", null, Role.BOARD),
                    new Endpoint(
                            HttpMethod.GET, "/api/v1/admin/members/" + SOME_ID, null, Role.BOARD),
                    new Endpoint(
                            HttpMethod.PUT,
                            "/api/v1/admin/members/" + SOME_ID + "/role",
                            "{\"role\": \"SETTER\"}",
                            Role.BOARD),
                    new Endpoint(
                            HttpMethod.PUT,
                            "/api/v1/admin/members/" + SOME_ID + "/status",
                            "{\"status\": \"DISABLED\"}",
                            Role.BOARD),
                    new Endpoint(
                            HttpMethod.POST,
                            "/api/v1/admin/members/" + SOME_ID + "/password-reset-link",
                            null,
                            Role.BOARD),
                    new Endpoint(HttpMethod.GET, "/api/v1/admin/audit", null, Role.BOARD),
                    new Endpoint(
                            HttpMethod.GET, "/api/v1/admin/registration-codes", null, Role.ADMIN),
                    new Endpoint(
                            HttpMethod.POST, "/api/v1/admin/registration-codes", "{}", Role.ADMIN),
                    new Endpoint(
                            HttpMethod.PUT,
                            "/api/v1/admin/registration-codes/" + SOME_ID,
                            "{\"label\": \"Affiche\", \"active\": true}",
                            Role.ADMIN));

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;

    static Stream<Arguments> everyEndpointForEveryCaller() {
        var cases = new ArrayList<Arguments>();
        for (var endpoint : ENDPOINTS) {
            cases.add(Arguments.of(endpoint, null));
            for (var role : Role.values()) {
                cases.add(Arguments.of(endpoint, role));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("everyEndpointForEveryCaller")
    void callersBelowTheRequiredRoleAreRefused(Endpoint endpoint, Role caller) throws Exception {
        var request =
                request(endpoint.method(), endpoint.path())
                        .with(xsrf())
                        .with(MemberFixtures.freshAddress());
        if (endpoint.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        if (caller != null) {
            request.with(MemberFixtures.as(members.create(caller)));
        }

        var status = mockMvc.perform(request).andReturn().getResponse().getStatus();

        if (caller == null) {
            assertThat(status).as("anonymous").isEqualTo(401);
        } else if (!caller.isAtLeast(endpoint.minimumRole())) {
            assertThat(status).as("below " + endpoint.minimumRole()).isEqualTo(403);
        } else {
            assertThat(status).as("allowed").isNotIn(401, 403).isLessThan(500);
        }
    }
}
