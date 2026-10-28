package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.FriendshipFixtures;
import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;

/**
 * The feed (spec §8.2): my sessions and the ones my friends share with friends, latest day first,
 * page by page. Never a "juste moi" session of a friend, never a stranger's, never a disabled
 * friend's.
 */
@IntegrationTest
class FeedIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private MemberRepository memberRepository;
    @Autowired private Clock clock;

    @Test
    void mineAndMyFriendsSharedOnesLatestDayFirst() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, me);
        var asking = members.create(Role.CLIMBER);
        friendships.ask(asking, me);
        var stranger = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));

        var mine = log(me, 3, SessionVisibility.PRIVATE, route);
        var friendShared = log(friend, 1, SessionVisibility.FRIENDS, route);
        log(friend, 2, SessionVisibility.PRIVATE, route);
        log(asking, 0, SessionVisibility.FRIENDS, route);
        log(stranger, 0, SessionVisibility.FRIENDS, route);

        mockMvc.perform(get("/api/v1/feed").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items[*].id")
                                .value(
                                        contains(
                                                friendShared.id().toString(),
                                                mine.id().toString())))
                .andExpect(jsonPath("$.items[0].author.nickname").value(friend.nickname()))
                .andExpect(jsonPath("$.items[0].editable").value(false))
                .andExpect(jsonPath("$.items[1].author.nickname").value(me.nickname()))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void pageByPage() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        var route = routes.create(members.create(Role.SETTER));
        var latest = log(friend, 0, SessionVisibility.FRIENDS, route);
        var middle = log(me, 1, SessionVisibility.FRIENDS, route);
        var oldest = log(friend, 2, SessionVisibility.FRIENDS, route);

        var first =
                mockMvc.perform(get("/api/v1/feed").param("size", "2").with(as(me)))
                        .andExpect(
                                jsonPath("$.items[*].id")
                                        .value(
                                                contains(
                                                        latest.id().toString(),
                                                        middle.id().toString())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        String cursor = JsonPath.read(first, "$.nextCursor");

        mockMvc.perform(get("/api/v1/feed").param("size", "2").param("cursor", cursor).with(as(me)))
                .andExpect(jsonPath("$.items[*].id").value(contains(oldest.id().toString())))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
        mockMvc.perform(get("/api/v1/feed").param("cursor", "pas-un-curseur").with(as(me)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aDisabledFriendLeavesTheFeed() throws Exception {
        var me = members.create(Role.CLIMBER);
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(me, friend);
        log(friend, 0, SessionVisibility.FRIENDS, routes.create(members.create(Role.SETTER)));
        friend.disable();
        memberRepository.saveAndFlush(friend);

        mockMvc.perform(get("/api/v1/feed").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    private ClimbingSession log(
            Member climber, int daysBack, SessionVisibility visibility, Route route) {
        return sessions.log(
                climber, today().minusDays(daysBack), 7, visibility, clean(route, ClimbMode.LEAD));
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }
}
