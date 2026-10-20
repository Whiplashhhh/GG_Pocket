package fr.gravelinesgrimp.pocket.admin.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.freshAddress;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

/** Member management by the board and the admin, with the role matrix of spec §5. */
@IntegrationTest
class MemberAdminIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;

    @Test
    void theBoardSeesTheMembersTable() throws Exception {
        var climber = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/admin/members").with(as(members.create(Role.BOARD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nickname", hasItem(climber.nickname())))
                .andExpect(jsonPath("$[0].registeredAt").exists());
    }

    @Test
    void theSheetHidesTheBirthDateAndListsAllowedActions() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/admin/members/" + climber.id()).with(as(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.nickname").value(climber.nickname()))
                .andExpect(jsonPath("$.birthDate").doesNotExist())
                .andExpect(jsonPath("$.underFifteenWithParentalConsent").value(false))
                .andExpect(jsonPath("$.permissions.assignableRoles[0]").value("SETTER"))
                .andExpect(jsonPath("$.permissions.canDisable").value(true))
                .andExpect(jsonPath("$.permissions.canCreateResetLink").value(true));
    }

    @Test
    void theBoardMakesAClimberASetterAndBack() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        changeRole(board, climber, Role.SETTER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.role").value("SETTER"));
        changeRole(board, climber, Role.CLIMBER).andExpect(status().isOk());
    }

    @Test
    void theBoardCannotHandOutBoardOrAdminRoles() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        changeRole(board, climber, Role.BOARD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        changeRole(board, climber, Role.ADMIN).andExpect(status().isForbidden());
    }

    @Test
    void theBoardCannotTouchOtherBoardMembersNorAdmins() throws Exception {
        var board = members.create(Role.BOARD);
        var otherBoard = members.create(Role.BOARD);
        var admin = members.create(Role.ADMIN);
        changeRole(board, otherBoard, Role.CLIMBER).andExpect(status().isForbidden());
        changeRole(board, admin, Role.CLIMBER).andExpect(status().isForbidden());
        changeStatus(board, admin, "DISABLED").andExpect(status().isForbidden());
        resetLink(board, otherBoard).andExpect(status().isForbidden());
    }

    @Test
    void theAdminGivesAnyRole() throws Exception {
        var admin = members.create(Role.ADMIN);
        var climber = members.create(Role.CLIMBER);
        changeRole(admin, climber, Role.BOARD).andExpect(status().isOk());
        changeRole(admin, climber, Role.ADMIN).andExpect(jsonPath("$.member.role").value("ADMIN"));
        changeRole(admin, climber, Role.CLIMBER).andExpect(status().isOk());
    }

    @Test
    void nobodyChangesTheirOwnRole() throws Exception {
        var admin = members.create(Role.ADMIN);
        changeRole(admin, admin, Role.CLIMBER)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SELF_MANAGEMENT_FORBIDDEN"));
        changeStatus(admin, admin, "DISABLED")
                .andExpect(jsonPath("$.code").value("SELF_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    void aNewRoleAppliesToTheOpenSessionAtOnce() throws Exception {
        var admin = members.create(Role.ADMIN);
        var climber = members.create(Role.CLIMBER);
        var session = MemberFixtures.signIn(mockMvc, climber, true);
        mockMvc.perform(get("/api/v1/admin/members").cookie(session))
                .andExpect(status().isForbidden());

        changeRole(admin, climber, Role.BOARD).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/members").cookie(session)).andExpect(status().isOk());
    }

    @Test
    void disablingAnAccountClosesItsSessionsAndBlocksSignIn() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        var session = MemberFixtures.signIn(mockMvc, climber, true);

        changeStatus(board, climber, "DISABLED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.status").value("DISABLED"))
                .andExpect(jsonPath("$.permissions.canEnable").value(true));

        mockMvc.perform(get("/api/v1/me").cookie(session)).andExpect(status().isUnauthorized());
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(xsrf())
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"nickname": "%s", "password": "%s", "rememberMe": true}
                                        """
                                                .formatted(
                                                        climber.nickname(),
                                                        MemberFixtures.PASSWORD)))
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));

        changeStatus(board, climber, "ACTIVE").andExpect(status().isOk());
        MemberFixtures.signIn(mockMvc, climber, true);
    }

    @Test
    void aResetLinkLetsTheMemberChooseANewPasswordOnce() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        var oldSession = MemberFixtures.signIn(mockMvc, climber, true);

        var token = tokenOf(resetLink(board, climber).andExpect(status().isOk()));
        mockMvc.perform(
                        post("/api/v1/auth/password-reset/check")
                                .with(xsrf())
                                .with(freshAddress())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"token\": \"%s\"}".formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(climber.nickname()));

        var newSession =
                reset(token, "Du mou du mou du mou")
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.nickname").value(climber.nickname()))
                        .andReturn()
                        .getResponse()
                        .getCookie(MemberFixtures.SESSION_COOKIE);

        assertThat(newSession).isNotNull();
        mockMvc.perform(get("/api/v1/me").cookie(newSession)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/me").cookie(oldSession)).andExpect(status().isUnauthorized());
        reset(token, "Encore un autre essai")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_LINK_INVALID"));
    }

    @Test
    void aNewResetLinkReplacesThePreviousOne() throws Exception {
        var board = members.create(Role.BOARD);
        var climber = members.create(Role.CLIMBER);
        var first = tokenOf(resetLink(board, climber));
        var second = tokenOf(resetLink(board, climber));

        reset(first, "Du mou du mou du mou")
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_LINK_INVALID"));
        reset(second, "Du mou du mou du mou").andExpect(status().isOk());
    }

    @Test
    void everyChangeIsInTheAuditLog() throws Exception {
        var admin = members.create(Role.ADMIN);
        var climber = members.create(Role.CLIMBER);
        changeRole(admin, climber, Role.SETTER);
        changeStatus(admin, climber, "DISABLED");

        var board = members.create(Role.BOARD);
        mockMvc.perform(get("/api/v1/admin/audit").param("size", "2").with(as(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].action").value("MEMBER_DISABLED"))
                .andExpect(jsonPath("$.entries[0].actorNickname").value(admin.nickname()))
                .andExpect(jsonPath("$.entries[0].targetLabel").value(climber.nickname()))
                .andExpect(jsonPath("$.entries[1].action").value("MEMBER_ROLE_CHANGED"))
                .andExpect(jsonPath("$.entries[1].before").value("CLIMBER"))
                .andExpect(jsonPath("$.entries[1].after").value("SETTER"))
                .andExpect(jsonPath("$.nextCursor").exists());
    }

    @Test
    void theAuditLogPagesWithACursor() throws Exception {
        var admin = members.create(Role.ADMIN);
        var climber = members.create(Role.CLIMBER);
        changeRole(admin, climber, Role.SETTER);
        changeRole(admin, climber, Role.CLIMBER);

        var firstPage = audit(admin, null);
        String cursor = JsonPath.read(firstPage, "$.nextCursor");
        String firstId = JsonPath.read(firstPage, "$.entries[0].id");
        var secondPage = audit(admin, cursor);
        String secondId = JsonPath.read(secondPage, "$.entries[0].id");

        assertThat(secondId).isNotEqualTo(firstId);
    }

    private String audit(Member reader, String cursor) throws Exception {
        var request = get("/api/v1/admin/audit").param("size", "1").with(as(reader));
        if (cursor != null) {
            request.param("cursor", cursor);
        }
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private ResultActions changeRole(Member actor, Member target, Role role) throws Exception {
        return mockMvc.perform(
                put("/api/v1/admin/members/" + target.id() + "/role")
                        .with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"%s\"}".formatted(role)));
    }

    private ResultActions changeStatus(Member actor, Member target, String status)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/admin/members/" + target.id() + "/status")
                        .with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"%s\"}".formatted(status)));
    }

    private ResultActions resetLink(Member actor, Member target) throws Exception {
        return mockMvc.perform(
                post("/api/v1/admin/members/" + target.id() + "/password-reset-link")
                        .with(as(actor))
                        .with(xsrf()));
    }

    private ResultActions reset(String token, String newPassword) throws Exception {
        return mockMvc.perform(
                post("/api/v1/auth/password-reset")
                        .with(xsrf())
                        .with(freshAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"token\": \"%s\", \"newPassword\": \"%s\"}"
                                        .formatted(token, newPassword)));
    }

    private static String tokenOf(ResultActions result) throws Exception {
        return JsonPath.read(
                result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                "$.token");
    }
}
