package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Reactions to sessions (spec §8.2): one per friend and session, changed in place or taken back;
 * never on my own session, never on a session I cannot read.
 */
@IntegrationTest
class SessionReactionIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void aFriendReactsChangesTheirMindAndTakesItBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        friendships.befriend(other, me);
        var session = shared(me);

        react(friend, session, "FIRE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts[0].kind").value("FIRE"))
                .andExpect(jsonPath("$.counts[0].count").value(1))
                .andExpect(jsonPath("$.counts[3].kind").value("GORILLA"))
                .andExpect(jsonPath("$.mine").value("FIRE"));
        react(other, session, "FIRE").andExpect(jsonPath("$.counts[0].count").value(2));
        // Twice the same: still one reaction.
        react(friend, session, "FIRE").andExpect(jsonPath("$.counts[0].count").value(2));
        react(friend, session, "GORILLA")
                .andExpect(jsonPath("$.counts[0].count").value(1))
                .andExpect(jsonPath("$.counts[3].count").value(1))
                .andExpect(jsonPath("$.mine").value("GORILLA"));

        // The session itself carries them, as each reader sees them.
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(me)))
                .andExpect(jsonPath("$.reactions.counts[0].count").value(1))
                .andExpect(jsonPath("$.reactions.counts[3].count").value(1))
                .andExpect(jsonPath("$.reactions.mine").doesNotExist());
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(other)))
                .andExpect(jsonPath("$.reactions.mine").value("FIRE"));

        takeBack(friend, session)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts[3].count").value(0))
                .andExpect(jsonPath("$.mine").doesNotExist());
        // Nothing left to take back: nothing happens.
        takeBack(friend, session).andExpect(status().isOk());
    }

    @Test
    void noReactionToMyOwnSession() throws Exception {
        var me = members.create(Role.CLIMBER);
        var session = shared(me);

        react(me, session, "STRONG").andExpect(status().isBadRequest());
    }

    @Test
    void noReactionWhereIDoNotRead() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var stranger = members.create(Role.CLIMBER);
        var admin = members.create(Role.ADMIN);
        var shared = shared(me);
        var hidden =
                sessions.log(
                        me,
                        today(),
                        6,
                        SessionVisibility.PRIVATE,
                        clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));

        react(friend, hidden, "CLAP").andExpect(status().isNotFound());
        takeBack(friend, hidden).andExpect(status().isNotFound());
        for (var reader : new Member[] {stranger, admin}) {
            react(reader, shared, "CLAP").andExpect(status().isNotFound());
            takeBack(reader, shared).andExpect(status().isNotFound());
        }
    }

    @Test
    void anUnknownKindIsRefused() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);

        react(friend, shared(me), "LIKE").andExpect(status().isBadRequest());
    }

    private ClimbingSession shared(Member climber) {
        return sessions.log(
                climber,
                today(),
                8,
                clean(routes.create(members.create(Role.SETTER)), ClimbMode.LEAD));
    }

    private ResultActions react(Member member, ClimbingSession session, String kind)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + session.id() + "/reaction")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\": \"" + kind + "\"}"));
    }

    private ResultActions takeBack(Member member, ClimbingSession session) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/sessions/" + session.id() + "/reaction")
                        .with(as(member))
                        .with(xsrf()));
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }
}
