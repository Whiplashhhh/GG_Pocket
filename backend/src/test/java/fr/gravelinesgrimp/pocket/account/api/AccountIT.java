package fr.gravelinesgrimp.pocket.account.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

/**
 * "Tes données" (spec §14): my data export, and deleting an account for good, by its owner or by
 * the admin, with the sessions and the routes handed over to "Ancien·ne membre" (D-096).
 */
@IntegrationTest
class AccountIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private MemberRepository memberRepository;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void myExportHoldsMyProfileSessionsAndRoutesSet() throws Exception {
        var me = members.create(Role.SETTER);
        var route = routes.create(me);
        sessions.log(me, LocalDate.now().minusDays(1), 7, clean(route, ClimbMode.LEAD));
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var theirs =
                sessions.log(friend, LocalDate.now().minusDays(1), 6, clean(route, ClimbMode.LEAD));
        say(me, theirs, "Bien joué");

        mockMvc.perform(get("/api/v1/me/export").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        containsString(
                                                "attachment; filename=\"gg-pocket-mes-donnees-")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.profile.nickname").value(me.nickname()))
                .andExpect(jsonPath("$.sessions[0].feeling").value(7))
                .andExpect(jsonPath("$.sessions[0].ascents[0].route.name").value(route.name()))
                .andExpect(jsonPath("$.sessions[0].ascents[0].clean").value(true))
                .andExpect(jsonPath("$.routesSet[0].id").value(route.id().toString()))
                .andExpect(jsonPath("$.friendships[0].nickname").value(friend.nickname()))
                .andExpect(jsonPath("$.friendships[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.friendships[0].askedByMe").value(false))
                .andExpect(jsonPath("$.sessionComments[0].body").value("Bien joué"))
                .andExpect(
                        jsonPath("$.sessionComments[0].session.climber").value(friend.nickname()))
                .andExpect(jsonPath("$.sessionReactions[0].kind").value("FIRE"))
                .andExpect(jsonPath("$.devices").isArray());
    }

    @Test
    void myExportHoldsNothingOfTheOthers() throws Exception {
        var me = members.create(Role.CLIMBER);
        var other = members.create(Role.SETTER);
        sessions.log(other, LocalDate.now(), 5, clean(routes.create(other), ClimbMode.TOP_ROPE));

        mockMvc.perform(get("/api/v1/me/export").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.nickname").value(me.nickname()))
                .andExpect(jsonPath("$.sessions").isEmpty())
                .andExpect(jsonPath("$.routesSet").isEmpty());
    }

    @Test
    void deletingMyAccountNeedsMyPassword() throws Exception {
        var me = members.create(Role.CLIMBER);
        deleteMine(as(me), "pas le bon")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CURRENT_PASSWORD_WRONG"));
        assertThat(memberRepository.findById(me.id())).isPresent();
    }

    @Test
    void deletingMyAccountHandsMySessionsAndRoutesOverToTheFormerMember() throws Exception {
        var me = members.create(Role.SETTER);
        var route = routes.create(me);
        var session =
                sessions.log(me, LocalDate.now().minusDays(2), 8, clean(route, ClimbMode.LEAD));
        mockMvc.perform(
                        multipart(HttpMethod.PUT, "/api/v1/me/avatar")
                                .file(png())
                                .with(as(me))
                                .with(xsrf()))
                .andExpect(status().isOk());
        var avatarId = memberRepository.findById(me.id()).orElseThrow().avatarId();
        assertThat(avatarId).isNotNull();
        var cookie = MemberFixtures.signIn(mockMvc, me, true);

        mockMvc.perform(
                        post("/api/v1/me/deletion")
                                .cookie(cookie)
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"password\": \"%s\"}"
                                                .formatted(MemberFixtures.PASSWORD)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Clear-Site-Data", "\"cache\""));

        assertThat(memberRepository.findById(me.id())).isEmpty();
        mockMvc.perform(get("/api/v1/me").cookie(cookie)).andExpect(status().isUnauthorized());
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from media_file where id = ?",
                                Integer.class,
                                avatarId))
                .isZero();
        var handedOver =
                jdbc.queryForMap(
                        "select member_id, visibility, comment, photo_id from climbing_session"
                                + " where id = ?",
                        session.id());
        assertThat(handedOver.get("member_id")).isEqualTo(FormerMember.ID);
        assertThat(handedOver.get("visibility")).isEqualTo("PRIVATE");
        assertThat(handedOver.get("comment")).isNull();
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from ascent where session_id = ?",
                                Integer.class,
                                session.id()))
                .isEqualTo(1);
        assertThat(
                        jdbc.queryForList(
                                "select member_id from route_setter where route_id = ?",
                                UUID.class,
                                route.id()))
                .containsExactly(FormerMember.ID);
    }

    @Test
    void deletingMyAccountTakesWhatTheOthersSaidOnMySessions() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var mine =
                sessions.log(
                        me,
                        LocalDate.now().minusDays(1),
                        8,
                        clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
        var theirs =
                sessions.log(
                        friend,
                        LocalDate.now().minusDays(1),
                        6,
                        clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
        say(friend, mine, "Bravo");
        say(me, theirs, "Merci");
        var cookie = MemberFixtures.signIn(mockMvc, me, true);

        mockMvc.perform(
                        post("/api/v1/me/deletion")
                                .cookie(cookie)
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"password\": \"%s\"}"
                                                .formatted(MemberFixtures.PASSWORD)))
                .andExpect(status().isNoContent());

        for (var table : List.of("session_reaction", "session_comment")) {
            assertThat(
                            jdbc.queryForObject(
                                    "select count(*) from " + table + " where session_id in (?, ?)",
                                    Integer.class,
                                    mine.id(),
                                    theirs.id()))
                    .as(table)
                    .isZero();
        }
    }

    /** A reaction and a comment from {@code member} on {@code session}. */
    private void say(Member member, ClimbingSession session, String comment) throws Exception {
        mockMvc.perform(
                        put("/api/v1/sessions/" + session.id() + "/reaction")
                                .with(as(member))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"kind\": \"FIRE\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/api/v1/sessions/" + session.id() + "/comments")
                                .with(as(member))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"body\": \"" + comment + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void theAdminDeletesAnAccountTheBoardCannot() throws Exception {
        var admin = members.create(Role.ADMIN);
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);

        mockMvc.perform(get("/api/v1/admin/members/" + climber.id()).with(as(board)))
                .andExpect(jsonPath("$.permissions.canDelete").value(false));
        deleteMember(board, climber.id()).andExpect(status().isForbidden());
        assertThat(memberRepository.findById(climber.id())).isPresent();

        mockMvc.perform(get("/api/v1/admin/members/" + climber.id()).with(as(admin)))
                .andExpect(jsonPath("$.permissions.canDelete").value(true));
        deleteMember(admin, climber.id()).andExpect(status().isNoContent());
        assertThat(memberRepository.findById(climber.id())).isEmpty();
        mockMvc.perform(get("/api/v1/admin/audit").with(as(admin)))
                .andExpect(jsonPath("$.entries[0].action").value("MEMBER_DELETED"))
                .andExpect(jsonPath("$.entries[0].targetLabel").value(climber.nickname()));
    }

    @Test
    void theAdminDoesNotDeleteTheirOwnAccountFromTheTable() throws Exception {
        var admin = members.create(Role.ADMIN);
        mockMvc.perform(get("/api/v1/admin/members/" + admin.id()).with(as(admin)))
                .andExpect(jsonPath("$.permissions.canDelete").value(false));
        deleteMember(admin, admin.id())
                .andExpect(jsonPath("$.code").value("SELF_MANAGEMENT_FORBIDDEN"));
        assertThat(memberRepository.findById(admin.id())).isPresent();
    }

    @Test
    void theFormerMemberIsOutOfReach() throws Exception {
        var admin = members.create(Role.ADMIN);
        mockMvc.perform(get("/api/v1/admin/members").with(as(admin)))
                .andExpect(jsonPath("$[*].id", not(hasItem(FormerMember.ID.toString()))));
        mockMvc.perform(get("/api/v1/admin/members/" + FormerMember.ID).with(as(admin)))
                .andExpect(status().isNotFound());
        deleteMember(admin, FormerMember.ID).andExpect(status().isNotFound());
        assertThat(memberRepository.findById(FormerMember.ID)).isPresent();
    }

    @Test
    void theMembersTableCountsTheSessions() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        sessions.log(climber, LocalDate.now().minusDays(3), 6, clean(route, ClimbMode.TOP_ROPE));
        sessions.log(climber, LocalDate.now().minusDays(1), 6, clean(route, ClimbMode.LEAD));

        mockMvc.perform(get("/api/v1/admin/members").with(as(members.create(Role.BOARD))))
                .andExpect(
                        jsonPath("$[?(@.id == '%s')].sessionCount".formatted(climber.id()))
                                .value(hasItem(2)));
        mockMvc.perform(
                        get("/api/v1/admin/members/" + climber.id())
                                .with(as(members.create(Role.BOARD))))
                .andExpect(jsonPath("$.member.sessionCount").value(2));
    }

    private ResultActions deleteMine(RequestPostProcessor who, String password) throws Exception {
        return mockMvc.perform(
                post("/api/v1/me/deletion")
                        .with(who)
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"%s\"}".formatted(password)));
    }

    private ResultActions deleteMember(Member actor, UUID memberId) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/admin/members/" + memberId).with(as(actor)).with(xsrf()));
    }

    private static MockMultipartFile png() throws Exception {
        var image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "photo.png", "image/png", output.toByteArray());
    }
}
