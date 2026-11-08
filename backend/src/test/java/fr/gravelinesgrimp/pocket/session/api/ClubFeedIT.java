package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import javax.imageio.ImageIO;

/**
 * The club feed, both ways (D-120): a member who takes part reads the sessions shared with the club
 * by the other members who take part, and only them; the others get nothing real. Every database of
 * this suite is shared, so the club feed is checked by what it holds and what it lacks.
 */
@IntegrationTest
class ClubFeedIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private FriendshipFixtures friendships;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private MemberRepository memberRepository;
    @Autowired private Clock clock;

    @Test
    void takingPartIsMyOwnChoiceAndNotBeforeValidation() throws Exception {
        var me = members.create(Role.CLIMBER);

        mockMvc.perform(get("/api/v1/feed/club").with(as(me)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CLUB_FEED_NOT_JOINED"));
        setClubFeed(me, true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inClubFeed").value(true));
        mockMvc.perform(get("/api/v1/feed/club").with(as(me))).andExpect(status().isOk());
        setClubFeed(me, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inClubFeed").value(false));
        mockMvc.perform(get("/api/v1/feed/club").with(as(me)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CLUB_FEED_NOT_JOINED"));

        var awaiting = members.createAwaitingValidation();
        setClubFeed(awaiting, true).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/feed/club").with(as(awaiting)))
                .andExpect(status().isForbidden());
    }

    @Test
    void theClubFeedHoldsTheClubSessionsOfTheMembersWhoTakePartOnly() throws Exception {
        var me = join(members.create(Role.CLIMBER));
        var other = join(members.create(Role.CLIMBER));
        var notTakingPart = members.create(Role.CLIMBER);
        var disabled = join(members.create(Role.CLIMBER));
        var awaiting = members.createAwaitingValidation();
        // Forced in the database: the API refuses it, the feed must still leave them out.
        awaiting.changeClubFeed(true);
        memberRepository.saveAndFlush(awaiting);
        var route = routes.create(members.create(Role.SETTER));

        var mine = log(me, 2, SessionVisibility.CLUB, route);
        var shared = log(other, 1, SessionVisibility.CLUB, route);
        var friendsOnly = log(other, 0, SessionVisibility.FRIENDS, route);
        var hidden = log(other, 0, SessionVisibility.PRIVATE, route);
        var outside = log(notTakingPart, 0, SessionVisibility.CLUB, route);
        var gone = log(disabled, 0, SessionVisibility.CLUB, route);
        var waiting = log(awaiting, 0, SessionVisibility.CLUB, route);
        disabled.disable();
        memberRepository.saveAndFlush(disabled);

        mockMvc.perform(get("/api/v1/feed/club").param("size", "30").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id").value(hasItems(id(shared), id(mine))))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(friendsOnly)))))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(hidden)))))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(outside)))))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(gone)))))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(waiting)))));
        // Not in the friends' feed of a stranger, even one who takes part.
        mockMvc.perform(get("/api/v1/feed").with(as(me)))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(shared)))));
    }

    @Test
    void theClubFeedGoesPageByPage() throws Exception {
        var me = join(members.create(Role.CLIMBER));
        var route = routes.create(members.create(Role.SETTER));
        log(me, 0, SessionVisibility.CLUB, route);
        log(me, 1, SessionVisibility.CLUB, route);

        var first =
                mockMvc.perform(get("/api/v1/feed/club").param("size", "1").with(as(me)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items", hasSize(1)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String firstId = JsonPath.read(first, "$.items[0].id");
        String cursor = JsonPath.read(first, "$.nextCursor");
        mockMvc.perform(
                        get("/api/v1/feed/club")
                                .param("size", "1")
                                .param("cursor", cursor)
                                .with(as(me)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(firstId))));
        mockMvc.perform(get("/api/v1/feed/club").param("cursor", "pas-un-curseur").with(as(me)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aMemberWhoTakesPartReadsReactsAndCommentsOnAClubSession() throws Exception {
        var climber = join(members.create(Role.CLIMBER));
        var reader = join(members.create(Role.CLIMBER));
        var session =
                log(climber, 0, SessionVisibility.CLUB, routes.create(members.create(Role.SETTER)));

        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("CLUB"))
                .andExpect(jsonPath("$.editable").value(false));
        react(reader, session).andExpect(status().isOk());
        comment(reader, session).andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/sessions/" + session.id() + "/comments").with(as(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author.nickname").value(reader.nickname()));
        mockMvc.perform(get("/api/v1/members/" + climber.id() + "/sessions").with(as(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id").value(contains(id(session))));
    }

    @Test
    void aMemberWhoDoesNotTakePartGetsNothing() throws Exception {
        var climber = join(members.create(Role.CLIMBER));
        var awaiting = members.createAwaitingValidation();
        var outsider = members.create(Role.CLIMBER);
        var session =
                log(climber, 0, SessionVisibility.CLUB, routes.create(members.create(Role.SETTER)));

        for (var reader : new Member[] {outsider, awaiting}) {
            mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(reader)))
                    .andExpect(status().isNotFound());
            react(reader, session).andExpect(status().isNotFound());
            comment(reader, session).andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/sessions/" + session.id() + "/comments").with(as(reader)))
                    .andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/members/" + climber.id() + "/sessions").with(as(reader)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void aClimberWhoLeavesTakesTheirClubSessionsBackToTheirFriends() throws Exception {
        var climber = join(members.create(Role.CLIMBER));
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(climber, friend);
        var reader = join(members.create(Role.CLIMBER));
        var session =
                log(climber, 0, SessionVisibility.CLUB, routes.create(members.create(Role.SETTER)));

        setClubFeed(climber, false).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(reader)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/feed/club").param("size", "30").with(as(reader)))
                .andExpect(jsonPath("$.items[*].id").value(not(hasItem(id(session)))));
        mockMvc.perform(get("/api/v1/members/" + climber.id() + "/sessions").with(as(reader)))
                .andExpect(status().isNotFound());
        // Nothing was rewritten: their friend still reads it, and it comes back with them.
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("CLUB"));
        setClubFeed(climber, true).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/sessions/" + session.id()).with(as(reader)))
                .andExpect(status().isOk());
    }

    @Test
    void aFriendWhoDoesNotTakePartStillReadsAClubSessionButNeverAPrivateOne() throws Exception {
        var climber = join(members.create(Role.CLIMBER));
        var friend = members.create(Role.CLIMBER);
        friendships.befriend(friend, climber);
        var clubMate = join(members.create(Role.CLIMBER));
        var route = routes.create(members.create(Role.SETTER));
        var shared = log(climber, 1, SessionVisibility.CLUB, route);
        var hidden = log(climber, 0, SessionVisibility.PRIVATE, route);

        mockMvc.perform(get("/api/v1/sessions/" + shared.id()).with(as(friend)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/feed").with(as(friend)))
                .andExpect(jsonPath("$.items[*].id").value(contains(id(shared))));
        mockMvc.perform(get("/api/v1/members/" + climber.id() + "/sessions").with(as(friend)))
                .andExpect(jsonPath("$.items[*].id").value(contains(id(shared))));
        mockMvc.perform(get("/api/v1/members/" + climber.id() + "/logbook").with(as(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.sessionCount").value(1));
        for (var reader : new Member[] {friend, clubMate}) {
            mockMvc.perform(get("/api/v1/sessions/" + hidden.id()).with(as(reader)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void onlyAMemberWhoTakesPartSharesASessionWithTheClub() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));

        create(me, route, "CLUB")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CLUB_FEED_NOT_JOINED"));
        var friendsOnly = log(me, 0, SessionVisibility.FRIENDS, route);
        privacy(me, friendsOnly, "CLUB")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CLUB_FEED_NOT_JOINED"));

        join(me);
        create(me, route, "CLUB")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("CLUB"));

        // After leaving, a session that has it keeps it; none gets it anew.
        var kept = log(me, 1, SessionVisibility.CLUB, route);
        setClubFeed(me, false).andExpect(status().isOk());
        privacy(me, kept, "CLUB").andExpect(status().isOk());
        privacy(me, friendsOnly, "CLUB").andExpect(status().isForbidden());
        privacy(me, kept, "FRIENDS").andExpect(status().isOk());
        privacy(me, kept, "CLUB").andExpect(status().isForbidden());
    }

    @Test
    void thePhotoOfAClubSessionFollowsTheSameRule() throws Exception {
        var climber = join(members.create(Role.CLIMBER));
        var reader = join(members.create(Role.CLIMBER));
        var outsider = members.create(Role.CLIMBER);
        var session =
                log(climber, 0, SessionVisibility.CLUB, routes.create(members.create(Role.SETTER)));
        var upload =
                multipart(HttpMethod.PUT, "/api/v1/sessions/" + session.id() + "/photo")
                        .file(png());
        upload.with(as(climber)).with(xsrf());
        var uploaded = mockMvc.perform(upload).andExpect(status().isOk()).andReturn();
        String photoUrl = JsonPath.read(uploaded.getResponse().getContentAsString(), "$.photoUrl");

        mockMvc.perform(get(photoUrl).with(as(reader))).andExpect(status().isOk());
        mockMvc.perform(get(photoUrl).with(as(outsider))).andExpect(status().isNotFound());
        setClubFeed(reader, false).andExpect(status().isOk());
        mockMvc.perform(get(photoUrl).with(as(reader))).andExpect(status().isNotFound());
    }

    private Member join(Member member) {
        member.changeClubFeed(true);
        return memberRepository.saveAndFlush(member);
    }

    private ResultActions setClubFeed(Member member, boolean inClubFeed) throws Exception {
        return mockMvc.perform(
                put("/api/v1/me/club-feed")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inClubFeed\": " + inClubFeed + "}"));
    }

    private ResultActions react(Member member, ClimbingSession session) throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + session.id() + "/reaction")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\": \"FIRE\"}"));
    }

    private ResultActions comment(Member member, ClimbingSession session) throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions/" + session.id() + "/comments")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"Belle croix !\"}"));
    }

    private ResultActions create(Member member, Route route, String visibility) throws Exception {
        return mockMvc.perform(
                post("/api/v1/sessions")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"clientRef": "%s", "climbedOn": "%s", "visibility": "%s",
                                 "ascents": [{"routeId": "%s", "mode": "LEAD", "topped": true,
                                  "clean": true, "falls": 0, "firstTry": false}]}
                                """
                                        .formatted(
                                                UUID.randomUUID(),
                                                today(),
                                                visibility,
                                                route.id())));
    }

    private ResultActions privacy(Member member, ClimbingSession session, String visibility)
            throws Exception {
        return mockMvc.perform(
                put("/api/v1/sessions/" + session.id() + "/privacy")
                        .with(as(member))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"visibility\": \"%s\", \"removeComment\": false}"
                                        .formatted(visibility)));
    }

    private ClimbingSession log(
            Member climber, int daysBack, SessionVisibility visibility, Route route) {
        return sessions.log(
                climber, today().minusDays(daysBack), 7, visibility, clean(route, ClimbMode.LEAD));
    }

    private static String id(ClimbingSession session) {
        return session.id().toString();
    }

    private LocalDate today() {
        return ClubTime.today(clock);
    }

    private static MockMultipartFile png() throws Exception {
        var image = new BufferedImage(64, 48, BufferedImage.TYPE_INT_RGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", "seance.png", "image/png", output.toByteArray());
    }
}
