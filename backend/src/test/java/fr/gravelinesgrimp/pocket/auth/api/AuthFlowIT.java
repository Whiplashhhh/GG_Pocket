package fr.gravelinesgrimp.pocket.auth.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.PASSWORD;
import static fr.gravelinesgrimp.pocket.MemberFixtures.SESSION_COOKIE;
import static fr.gravelinesgrimp.pocket.MemberFixtures.freshAddress;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCodeFormat;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Registration, sign-in and sign-out, through the real endpoints and security chain. */
@IntegrationTest
class AuthFlowIT {

    private static final AtomicInteger NICKNAMES = new AtomicInteger();

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RegistrationCodeRepository codes;

    @Test
    void anAnonymousVisitorGetsItsStateAndACsrfCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.member").doesNotExist())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void joiningWithACodeSignsInAtOnce() throws Exception {
        var code = newCode(null);
        var nickname = nickname();

        var response =
                register(code, nickname, PASSWORD, null, false)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.nickname").value(nickname))
                        .andExpect(jsonPath("$.role").value("CLIMBER"))
                        .andExpect(jsonPath("$.avatarUrl").doesNotExist())
                        .andReturn()
                        .getResponse();

        var session = response.getCookie(SESSION_COOKIE);
        assertThat(session).isNotNull();
        assertThat(session.isHttpOnly()).isTrue();
        assertThat(session.getSecure()).isTrue();
        assertThat(sessionSetCookie(response)).contains("SameSite=Lax");
        mockMvc.perform(get("/api/v1/auth/state").cookie(session))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.member.nickname").value(nickname));
        assertThat(codes.findByCode(code))
                .get()
                .extracting(RegistrationCode::useCount)
                .isEqualTo(1);
    }

    @Test
    void aWrongCodeIsRefusedAtBothSteps() throws Exception {
        check("ZZZZ-ZZZZ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REGISTRATION_CODE_INVALID"));
        register("ZZZZZZZZ", nickname(), PASSWORD, null, false)
                .andExpect(jsonPath("$.code").value("REGISTRATION_CODE_INVALID"));
    }

    @Test
    void theCodeCheckForgivesCaseAndSpaces() throws Exception {
        var code = newCode(null);
        check(" " + RegistrationCodeFormat.display(code).toLowerCase() + " ")
                .andExpect(status().isNoContent());
    }

    @Test
    void aCodeStopsWorkingAfterItsLastUse() throws Exception {
        var code = newCode(1);
        register(code, nickname(), PASSWORD, null, false).andExpect(status().isCreated());
        register(code, nickname(), PASSWORD, null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REGISTRATION_CODE_INVALID"));
    }

    @Test
    void commonPasswordsAreRefused() throws Exception {
        register(newCode(null), nickname(), "azertyuiop", null, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_TOO_COMMON"));
    }

    @Test
    void underFifteenNeedsAParentalConsent() throws Exception {
        var code = newCode(null);
        var tenYearsOld = LocalDate.now().minusYears(10);
        register(code, nickname(), PASSWORD, tenYearsOld, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARENTAL_CONSENT_REQUIRED"));
        var nickname = nickname();
        register(code, nickname, PASSWORD, tenYearsOld, true).andExpect(status().isCreated());
        assertThat(memberRepository.findByNicknameIgnoreCase(nickname))
                .get()
                .satisfies(member -> assertThat(member.hasParentalConsent()).isTrue());
    }

    @Test
    void nicknamesAreUniqueWhateverTheCase() throws Exception {
        var existing = members.create(Role.CLIMBER);
        register(newCode(null), existing.nickname().toUpperCase(), PASSWORD, null, false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NICKNAME_TAKEN"));
    }

    @Test
    void thePrivacyPageMustBeAccepted() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .with(xsrf())
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"code": "%s", "firstName": "Léa", "lastName": "Dupont",
                                         "nickname": "%s", "password": "%s",
                                         "privacyAccepted": false}
                                        """
                                                .formatted(newCode(null), nickname(), PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("privacyAccepted"));
    }

    @Test
    void rememberedSessionsOutliveTheBrowserOthersDoNot() throws Exception {
        var member = members.create(Role.CLIMBER);

        var remembered = login(member.nickname(), PASSWORD, true).andReturn().getResponse();
        assertThat(sessionSetCookie(remembered)).contains("Max-Age=");

        var browserOnly = login(member.nickname(), PASSWORD, false).andReturn().getResponse();
        assertThat(sessionSetCookie(browserOnly)).doesNotContain("Max-Age=");
    }

    @Test
    void wrongPasswordAndUnknownNicknameLookTheSame() throws Exception {
        var member = members.create(Role.CLIMBER);
        login(member.nickname(), "pas le bon mot de passe", true)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
        login("personne-ici", PASSWORD, true)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void theNicknameIsCaseInsensitiveAtSignIn() throws Exception {
        var member = members.create(Role.CLIMBER);
        login(member.nickname().toUpperCase(), PASSWORD, true).andExpect(status().isOk());
    }

    @Test
    void aDisabledAccountIsToldSoOnlyWithTheRightPassword() throws Exception {
        var member = members.create(Role.CLIMBER);
        member.disable();
        memberRepository.saveAndFlush(member);

        login(member.nickname(), PASSWORD, true)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
        login(member.nickname(), "pas le bon mot de passe", true)
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void signingOutClosesTheSession() throws Exception {
        var session = MemberFixtures.signIn(mockMvc, members.create(Role.CLIMBER), true);
        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/logout").with(xsrf()).cookie(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void signingInAgainGivesANewSessionId() throws Exception {
        var member = members.create(Role.CLIMBER);
        var first = MemberFixtures.signIn(mockMvc, member, true);
        var second =
                login(member.nickname(), PASSWORD, true)
                        .andReturn()
                        .getResponse()
                        .getCookie(SESSION_COOKIE);
        assertThat(second).isNotNull();
        assertThat(second.getValue()).isNotEqualTo(first.getValue());
    }

    @Test
    void writesWithoutTheCsrfHeaderAreRefused() throws Exception {
        var member = members.create(Role.CLIMBER);
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(credentials(member.nickname(), PASSWORD, true)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"));
    }

    private ResultActions login(String nickname, String password, boolean remember)
            throws Exception {
        return mockMvc.perform(
                post("/api/v1/auth/login")
                        .with(xsrf())
                        .with(freshAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(nickname, password, remember)));
    }

    private static String credentials(String nickname, String password, boolean remember) {
        return """
        {"nickname": "%s", "password": "%s", "rememberMe": %s}
        """
                .formatted(nickname, password, remember);
    }

    private ResultActions check(String code) throws Exception {
        return mockMvc.perform(
                post("/api/v1/auth/registration-code/check")
                        .with(xsrf())
                        .with(freshAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"%s\"}".formatted(code)));
    }

    private ResultActions register(
            String code,
            String nickname,
            String password,
            LocalDate birthDate,
            boolean parentalConsent)
            throws Exception {
        var birth = birthDate == null ? "null" : "\"" + birthDate + "\"";
        return mockMvc.perform(
                post("/api/v1/auth/register")
                        .with(xsrf())
                        .with(freshAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"code": "%s", "firstName": "Léa", "lastName": "Dupont",
                                 "nickname": "%s", "email": null, "password": "%s",
                                 "birthDate": %s, "parentalConsent": %s,
                                 "privacyAccepted": true, "rememberMe": true}
                                """
                                        .formatted(
                                                code, nickname, password, birth, parentalConsent)));
    }

    /** A club code, in its stored form. */
    private String newCode(Integer maxUses) {
        var value = RegistrationCodeFormat.generate(new SecureRandom());
        codes.saveAndFlush(
                new RegistrationCode(value, "Affiche de test", Role.CLIMBER, null, maxUses, null));
        return value;
    }

    private static String nickname() {
        return "nouveau" + NICKNAMES.incrementAndGet();
    }

    /** The raw Set-Cookie header of the session cookie (MockMvc drops SameSite from Cookie). */
    static String sessionSetCookie(MockHttpServletResponse response) {
        return response.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(header -> header.startsWith(SESSION_COOKIE + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No session cookie was set"));
    }
}
