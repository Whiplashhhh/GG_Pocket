package fr.gravelinesgrimp.pocket.social.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.MemberDetails;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.topo.domain.Favorite;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

/** The directory and the public profiles: what every member sees of the others, and no more. */
@IntegrationTest
class DirectoryIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private MemberRepository memberRepository;
    @Autowired private FavoriteRepository favorites;

    @Test
    void theDirectoryListsActiveMembersWithWhereIStandWithThem() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        var asked = members.create(Role.SETTER);
        var asking = members.create(Role.CLIMBER);
        var stranger = members.create(Role.CLIMBER);
        var disabled = members.create(Role.CLIMBER);
        disabled.disable();
        memberRepository.saveAndFlush(disabled);
        friendships.befriend(friend, me);
        friendships.ask(me, asked);
        friendships.ask(asking, me);

        mockMvc.perform(get("/api/v1/members").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.climberCount").value(greaterThanOrEqualTo(5)))
                .andExpect(state(friend.id(), "FRIENDS"))
                .andExpect(state(asked.id(), "REQUESTED"))
                .andExpect(state(asking.id(), "INCOMING"))
                .andExpect(state(stranger.id(), "NONE"))
                .andExpect(
                        jsonPath(entry(asked.id()) + ".member.firstName")
                                .value(contains(asked.firstName())))
                .andExpect(jsonPath(entry(asked.id()) + ".member.role").value(contains("SETTER")))
                .andExpect(jsonPath(entry(me.id()), empty()))
                .andExpect(jsonPath(entry(disabled.id()), empty()))
                .andExpect(jsonPath(entry(FormerMember.ID), empty()));
    }

    @Test
    void aProfileShowsThePublicPartOnly() throws Exception {
        var me = members.create(Role.CLIMBER);
        var other = members.create(Role.CLIMBER);
        other.describe(new MemberDetails(2019, 2012, "Spécialiste des dalles", false, true));
        other = memberRepository.saveAndFlush(other);
        var route = routes.create(members.create(Role.SETTER));
        favorites.saveAndFlush(new Favorite(other.id(), route.id()));

        mockMvc.perform(get("/api/v1/members/" + other.id()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.nickname").value(other.nickname()))
                .andExpect(jsonPath("$.member.clubArrivalYear").value(2019))
                .andExpect(jsonPath("$.climbingSinceYear").value(2012))
                .andExpect(jsonPath("$.bio").value("Spécialiste des dalles"))
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.member.email").doesNotExist())
                .andExpect(jsonPath("$.birthDate").doesNotExist())
                .andExpect(jsonPath("$.favorites[*].id").value(contains(route.id().toString())))
                .andExpect(jsonPath("$.friendship").value("NONE"));

        // The age shows once they choose to show it.
        other.describe(new MemberDetails(2019, 2012, null, true, true));
        memberRepository.saveAndFlush(other);
        mockMvc.perform(get("/api/v1/members/" + other.id()).with(as(me)))
                .andExpect(jsonPath("$.age").isNumber());

        mockMvc.perform(get("/api/v1/members/" + me.id()).with(as(me)))
                .andExpect(jsonPath("$.friendship").value("SELF"));
    }

    @Test
    void disabledFormerAndUnknownMembersHaveNoProfile() throws Exception {
        var me = members.create(Role.CLIMBER);
        var disabled = members.create(Role.CLIMBER);
        disabled.disable();
        memberRepository.saveAndFlush(disabled);

        for (var id : new UUID[] {disabled.id(), FormerMember.ID, UUID.randomUUID()}) {
            mockMvc.perform(get("/api/v1/members/" + id).with(as(me)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void theDirectoryIsForSignedInMembersOnly() throws Exception {
        var other = members.create(Role.CLIMBER);
        mockMvc.perform(get("/api/v1/members")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/members/" + other.id())).andExpect(status().isUnauthorized());
    }

    private static String entry(UUID id) {
        return "$.members[?(@.member.id == '" + id + "')]";
    }

    private static ResultMatcher state(UUID id, String friendship) {
        return jsonPath(entry(id) + ".friendship").value(contains(friendship));
    }
}
