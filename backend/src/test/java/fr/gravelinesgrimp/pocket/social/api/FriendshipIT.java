package fr.gravelinesgrimp.pocket.social.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

/** Friend requests: asked, accepted, declined, cancelled, ended; always from my own side. */
@IntegrationTest
class FriendshipIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private MemberRepository memberRepository;

    @Test
    void aRequestWaitsForTheOtherOneWhoAcceptsIt() throws Exception {
        var me = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);

        befriend(me, other)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendship").value("REQUESTED"));
        mockMvc.perform(get("/api/v1/me/friends").with(as(me)))
                .andExpect(jsonPath("$.outgoing[0].member.id").value(other.id().toString()))
                .andExpect(jsonPath("$.outgoing[0].member.nickname").value(other.nickname()))
                .andExpect(jsonPath("$.incoming", empty()))
                .andExpect(jsonPath("$.friends", empty()));
        mockMvc.perform(get("/api/v1/me/friends").with(as(other)))
                .andExpect(jsonPath("$.incoming[0].member.id").value(me.id().toString()))
                .andExpect(jsonPath("$.incoming[0].since").exists());

        // Asking again changes nothing.
        befriend(me, other).andExpect(jsonPath("$.friendship").value("REQUESTED"));

        befriend(other, me)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendship").value("FRIENDS"));
        for (var side : new Member[] {me, other}) {
            mockMvc.perform(get("/api/v1/me/friends").with(as(side)))
                    .andExpect(jsonPath("$.incoming", empty()))
                    .andExpect(jsonPath("$.outgoing", empty()))
                    .andExpect(jsonPath("$.friends", hasSize(1)));
        }
        befriend(me, other).andExpect(jsonPath("$.friendship").value("FRIENDS"));
    }

    @Test
    void decliningCancellingAndEndingAllRemoveTheLink() throws Exception {
        var me = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);

        // Declined by the one who was asked.
        befriend(me, other);
        unfriend(other, me).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me/friends").with(as(me)))
                .andExpect(jsonPath("$.outgoing", empty()));

        // Cancelled by the one who asked.
        befriend(me, other);
        unfriend(me, other).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me/friends").with(as(other)))
                .andExpect(jsonPath("$.incoming", empty()));

        // Ended by either friend, then asked again.
        befriend(me, other);
        befriend(other, me);
        unfriend(other, me).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me/friends").with(as(me)))
                .andExpect(jsonPath("$.friends", empty()));
        befriend(other, me).andExpect(jsonPath("$.friendship").value("REQUESTED"));

        // Nothing between us is fine too.
        unfriend(me, members.create(Role.CLIMBER)).andExpect(status().isNoContent());
    }

    @Test
    void onlyActiveMembersCanBeAsked() throws Exception {
        var me = members.create(Role.CLIMBER);
        var disabled = members.create(Role.CLIMBER);
        disabled.disable();
        memberRepository.saveAndFlush(disabled);

        befriend(me, me).andExpect(status().isBadRequest());
        for (var id : new UUID[] {disabled.id(), FormerMember.ID, UUID.randomUUID()}) {
            mockMvc.perform(put("/api/v1/me/friends/" + id).with(as(me)).with(xsrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }
    }

    @Test
    void aDisabledFriendDisappearsFromMyListUntilTheyComeBack() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        befriend(me, friend);
        befriend(friend, me);
        befriend(me, other);
        befriend(other, me);

        friend.disable();
        memberRepository.saveAndFlush(friend);
        mockMvc.perform(get("/api/v1/me/friends").with(as(me)))
                .andExpect(
                        jsonPath("$.friends[*].member.id").value(contains(other.id().toString())));
    }

    @Test
    void friendshipsNeedASignedInMember() throws Exception {
        var other = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/me/friends")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/me/friends/" + other.id()).with(xsrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/me/friends/" + other.id()).with(xsrf()))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions befriend(Member me, Member other) throws Exception {
        return mockMvc.perform(put("/api/v1/me/friends/" + other.id()).with(as(me)).with(xsrf()));
    }

    private ResultActions unfriend(Member me, Member other) throws Exception {
        return mockMvc.perform(
                delete("/api/v1/me/friends/" + other.id()).with(as(me)).with(xsrf()));
    }
}
