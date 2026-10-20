package fr.gravelinesgrimp.pocket.auth.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.PASSWORD;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/** Long sessions (spec §6) and "Mes appareils". */
@IntegrationTest
class SessionsIT {

    private static final String ANDROID_CHROME =
            "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/141.0.0.0 Mobile Safari/537.36";

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void aRememberedSessionLastsOneHundredEightyDaysOfInactivity() throws Exception {
        var session = MemberFixtures.signIn(mockMvc, members.create(Role.CLIMBER), true);
        assertThat(maxInactiveInterval(session)).isEqualTo(Duration.ofDays(180));

        lastActivity(session, Duration.ofDays(30));
        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isOk());

        // That visit pushed the deadline back: 179 days after it, still signed in.
        lastActivity(session, Duration.ofDays(179));
        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isOk());

        lastActivity(session, Duration.ofDays(181));
        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void withoutRememberMeTheSessionIsShort() throws Exception {
        var session = MemberFixtures.signIn(mockMvc, members.create(Role.CLIMBER), false);
        assertThat(maxInactiveInterval(session)).isEqualTo(Duration.ofHours(12));
    }

    @Test
    void myDevicesListsMySessionsAndMarksThisOne() throws Exception {
        var member = members.create(Role.CLIMBER);
        var phone = MemberFixtures.signIn(mockMvc, member, true);
        MemberFixtures.signIn(mockMvc, member.nickname(), PASSWORD, true, ANDROID_CHROME);

        mockMvc.perform(get("/api/v1/me/devices").cookie(phone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.current == true)].platform").value("IPHONE"))
                .andExpect(jsonPath("$[?(@.current == false)].platform").value("ANDROID"))
                .andExpect(jsonPath("$[?(@.current == false)].browser").value("CHROME"));
    }

    @Test
    void closingADeviceSignsItOut() throws Exception {
        var member = members.create(Role.CLIMBER);
        var phone = MemberFixtures.signIn(mockMvc, member, true);
        var tablet =
                MemberFixtures.signIn(mockMvc, member.nickname(), PASSWORD, true, ANDROID_CHROME);
        var tabletId = otherDeviceIds(phone).getFirst();

        mockMvc.perform(delete("/api/v1/me/devices/" + tabletId).with(csrf()).cookie(phone))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me").cookie(tablet)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me").cookie(phone)).andExpect(status().isOk());
    }

    @Test
    void nobodyClosesTheDeviceOfSomeoneElse() throws Exception {
        var victim = MemberFixtures.signIn(mockMvc, members.create(Role.CLIMBER), true);
        var victimDevice = JsonPath.<List<String>>read(devicesJson(victim), "$[*].id").getFirst();
        var attacker = MemberFixtures.signIn(mockMvc, members.create(Role.ADMIN), true);

        mockMvc.perform(delete("/api/v1/me/devices/" + victimDevice).with(csrf()).cookie(attacker))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/me").cookie(victim)).andExpect(status().isOk());
    }

    @Test
    void closeOthersKeepsOnlyThisDevice() throws Exception {
        var member = members.create(Role.CLIMBER);
        var phone = MemberFixtures.signIn(mockMvc, member, true);
        var laptop = MemberFixtures.signIn(mockMvc, member, true);
        var tablet = MemberFixtures.signIn(mockMvc, member, false);

        mockMvc.perform(post("/api/v1/me/devices/close-others").with(csrf()).cookie(phone))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me").cookie(laptop)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me").cookie(tablet)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me/devices").cookie(phone))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void changingMyPasswordClosesMyOtherSessions() throws Exception {
        var member = members.create(Role.CLIMBER);
        var phone = MemberFixtures.signIn(mockMvc, member, true);
        var laptop = MemberFixtures.signIn(mockMvc, member, true);

        mockMvc.perform(
                        put("/api/v1/me/password")
                                .with(csrf())
                                .cookie(phone)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"currentPassword": "%s", "newPassword": "Avant-bras en béton"}
                                        """
                                                .formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me").cookie(phone)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/me").cookie(laptop)).andExpect(status().isUnauthorized());
        MemberFixtures.signIn(
                mockMvc,
                member.nickname(),
                "Avant-bras en béton",
                true,
                MemberFixtures.IPHONE_SAFARI);
    }

    @Test
    void theCurrentPasswordIsRequiredToChangeIt() throws Exception {
        var phone = MemberFixtures.signIn(mockMvc, members.create(Role.CLIMBER), true);
        mockMvc.perform(
                        put("/api/v1/me/password")
                                .with(csrf())
                                .cookie(phone)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"currentPassword": "devine", "newPassword": "Avant-bras en béton"}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CURRENT_PASSWORD_WRONG"));
    }

    private String devicesJson(Cookie session) throws Exception {
        return mockMvc.perform(get("/api/v1/me/devices").cookie(session))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private List<String> otherDeviceIds(Cookie session) throws Exception {
        return JsonPath.read(devicesJson(session), "$[?(@.current == false)].id");
    }

    private static String sessionId(Cookie session) {
        return new String(Base64.getDecoder().decode(session.getValue()), StandardCharsets.UTF_8);
    }

    private Duration maxInactiveInterval(Cookie session) {
        var seconds =
                jdbc.queryForObject(
                        "select max_inactive_interval from spring_session where session_id = ?",
                        Integer.class,
                        sessionId(session));
        return Duration.ofSeconds(seconds);
    }

    /** Pretends the last visit of this session happened {@code ago}. */
    private void lastActivity(Cookie session, Duration ago) {
        var updated =
                jdbc.update(
                        "update spring_session set last_access_time = ?,"
                                + " expiry_time = ? + max_inactive_interval * 1000::bigint"
                                + " where session_id = ?",
                        Instant.now().minus(ago).toEpochMilli(),
                        Instant.now().minus(ago).toEpochMilli(),
                        sessionId(session));
        assertThat(updated).isEqualTo(1);
    }
}
