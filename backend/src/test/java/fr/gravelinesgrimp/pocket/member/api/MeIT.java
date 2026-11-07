package fr.gravelinesgrimp.pocket.member.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;

import javax.imageio.ImageIO;

/** My profile and photo. */
@IntegrationTest
class MeIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private MemberRepository memberRepository;

    @Test
    void iReadMyWholeProfile() throws Exception {
        var me = members.create(Role.SETTER);
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(me.nickname()))
                .andExpect(jsonPath("$.role").value("SETTER"))
                .andExpect(jsonPath("$.showAge").value(false))
                .andExpect(jsonPath("$.appearsInRankings").value(true))
                .andExpect(jsonPath("$.age").isNumber());
    }

    @Test
    void iEditMyProfile() throws Exception {
        var me = members.create(Role.CLIMBER);
        update(
                        me,
                        me.nickname() + "x",
                        "1995-03-04",
                        false,
                        "2019",
                        "Grimpe en dévers, rêve de dalle.")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(me.nickname() + "x"))
                .andExpect(jsonPath("$.birthDate").value("1995-03-04"))
                .andExpect(jsonPath("$.clubArrivalYear").value(2019))
                .andExpect(jsonPath("$.bio").value("Grimpe en dévers, rêve de dalle."));
    }

    @Test
    void anotherMembersNicknameIsTaken() throws Exception {
        var me = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        update(me, other.nickname().toLowerCase(), null, false, null, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NICKNAME_TAKEN"));
    }

    @Test
    void yearsCannotBeInTheFuture() throws Exception {
        var me = members.create(Role.CLIMBER);
        var nextYear = String.valueOf(Year.now().getValue() + 1);
        update(me, me.nickname(), null, false, nextYear, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("YEAR_IN_THE_FUTURE"));
    }

    @Test
    void aBirthDateUnderFifteenNeedsTheParentalConsent() throws Exception {
        var me = members.create(Role.CLIMBER);
        var birth = LocalDate.now().minusYears(12).toString();
        update(me, me.nickname(), birth, false, null, null)
                .andExpect(jsonPath("$.code").value("PARENTAL_CONSENT_REQUIRED"));
        update(me, me.nickname(), birth, true, null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentalConsent").value(true));
    }

    /** D-119: required, so it can be corrected but no longer removed. */
    @Test
    void myBirthDateCannotBeRemoved() throws Exception {
        var me = members.create(Role.CLIMBER);
        mockMvc.perform(
                        put("/api/v1/me/profile")
                                .with(as(me))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"nickname": "%s", "firstName": "Léa",
                                         "lastName": "Dupont", "birthDate": null,
                                         "parentalConsent": false,
                                         "showAge": false, "appearsInRankings": true}
                                        """
                                                .formatted(me.nickname())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("birthDate"));
    }

    /** D-119: an older account without birth date gives it at its next sign-in. */
    @Test
    void anOlderAccountGivesItsMissingBirthDate() throws Exception {
        var me = withoutBirthDate(members.create(Role.CLIMBER));
        var session = MemberFixtures.signIn(mockMvc, me, false);
        mockMvc.perform(get("/api/v1/auth/state").cookie(session))
                .andExpect(jsonPath("$.member.birthDateMissing").value(true));

        setBirthDate(me, "1990-05-06", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(me.nickname()))
                .andExpect(jsonPath("$.birthDateMissing").value(false));
        mockMvc.perform(get("/api/v1/auth/state").cookie(session))
                .andExpect(jsonPath("$.member.birthDateMissing").value(false));
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(jsonPath("$.birthDate").value("1990-05-06"));
    }

    @Test
    void aMissingBirthDateUnderFifteenNeedsTheParentalConsentToo() throws Exception {
        var me = withoutBirthDate(members.create(Role.CLIMBER));
        var birth = LocalDate.now().minusYears(12).toString();
        setBirthDate(me, birth, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARENTAL_CONSENT_REQUIRED"));
        setBirthDate(me, birth, true).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(jsonPath("$.parentalConsent").value(true));
    }

    @Test
    void theMissingBirthDateIsAnActualPastDate() throws Exception {
        var me = withoutBirthDate(members.create(Role.CLIMBER));
        setBirthDate(me, LocalDate.now().plusDays(2).toString(), false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void myPhotoIsCleanedStoredAndServedToMembersOnly() throws Exception {
        var me = members.create(Role.CLIMBER);
        var profile =
                mockMvc.perform(upload(me, png(900, 700)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.avatarUrl").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String avatarUrl = JsonPath.read(profile, "$.avatarUrl");

        var image =
                mockMvc.perform(get(avatarUrl).with(as(members.create(Role.CLIMBER))))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                        .andExpect(
                                header().string(
                                                HttpHeaders.CACHE_CONTROL,
                                                containsString("private")))
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();
        var decoded = ImageIO.read(new ByteArrayInputStream(image));
        assertThat(decoded.getWidth()).isEqualTo(512);
        assertThat(decoded.getHeight()).isEqualTo(512);

        mockMvc.perform(get(avatarUrl)).andExpect(status().isUnauthorized());
    }

    @Test
    void anythingButAnImageIsRefused() throws Exception {
        var me = members.create(Role.CLIMBER);
        var fake =
                new MockMultipartFile(
                        "file",
                        "photo.jpg",
                        "image/jpeg",
                        "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(upload(me, fake))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_UNSUPPORTED"));
    }

    /** Not a way to test which nicknames or e-mails belong to members (D-112). */
    @Test
    void profileChangesAreSlowedDownAfter20AnHour() throws Exception {
        var me = members.create(Role.CLIMBER);
        for (var i = 1; i <= 20; i++) {
            update(me, me.nickname(), null, false, null, null).andExpect(status().isOk());
        }
        update(me, me.nickname(), null, false, null, null)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void iRemoveMyPhoto() throws Exception {
        var me = members.create(Role.CLIMBER);
        mockMvc.perform(upload(me, png(300, 300))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/me/avatar").with(as(me)).with(xsrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me").with(as(me)))
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
    }

    private Member withoutBirthDate(Member member) {
        member.changeBirthDate(null, false, Instant.now());
        return memberRepository.saveAndFlush(member);
    }

    private ResultActions setBirthDate(Member me, String birthDate, boolean consent)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/birth-date")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"birthDate": "%s", "parentalConsent": %s}
                                """
                                        .formatted(birthDate, consent)));
    }

    private ResultActions update(
            Member me, String nickname, String birthDate, boolean consent, String year, String bio)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/profile")
                        .with(as(me))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nickname": "%s", "firstName": "Léa", "lastName": "Dupont",
                                 "email": "lea.%s@example.org", "birthDate": %s,
                                 "parentalConsent": %s, "showAge": false,
                                 "clubArrivalYear": %s, "climbingSinceYear": null,
                                 "bio": %s, "appearsInRankings": true}
                                """
                                        .formatted(
                                                nickname,
                                                me.id(),
                                                birthDate == null
                                                        ? "\"1990-01-01\""
                                                        : "\"" + birthDate + "\"",
                                                consent,
                                                year == null ? "null" : year,
                                                bio == null ? "null" : "\"" + bio + "\"")));
    }

    private static MockMultipartHttpServletRequestBuilder upload(
            Member me, MockMultipartFile file) {
        var request = multipart(HttpMethod.PUT, "/api/v1/me/avatar").file(file);
        request.with(as(me)).with(xsrf());
        return request;
    }

    private static MockMultipartFile png(int width, int height) throws Exception {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "photo.png", "image/png", output.toByteArray());
    }
}
