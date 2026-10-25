package fr.gravelinesgrimp.pocket.session.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;
import static fr.gravelinesgrimp.pocket.SessionFixtures.topped;
import static fr.gravelinesgrimp.pocket.SessionFixtures.tried;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

/** My logbook: everything I climbed, dismounted routes included, nobody else's. */
@IntegrationTest
class LogbookIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void addsUpEverythingIClimbed() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var sixA = routes.create(setter, "6a");
        var sixB = routes.create(setter, "6b");
        var gone = routes.create(setter, RouteStatus.DISMOUNTED);
        var today = ClubTime.today(clock);
        sessions.log(
                me,
                today.minusMonths(4),
                null,
                clean(gone, ClimbMode.LEAD),
                topped(sixA, ClimbMode.TOP_ROPE));
        sessions.log(
                me,
                today,
                8,
                clean(sixA, ClimbMode.LEAD),
                tried(sixB, ClimbMode.LEAD),
                topped(sixB, ClimbMode.TOP_ROPE));

        mockMvc.perform(get("/api/v1/me/logbook").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.sessionCount").value(2))
                .andExpect(jsonPath("$.totals.climbingDays").value(2))
                .andExpect(jsonPath("$.totals.ascentCount").value(5))
                .andExpect(jsonPath("$.totals.toppedCount").value(4))
                .andExpect(jsonPath("$.totals.leadToppedCount").value(2))
                .andExpect(jsonPath("$.totals.cleanCount").value(2))
                .andExpect(jsonPath("$.totals.routeCount").value(3))
                .andExpect(jsonPath("$.totals.maxGrade").value("6b"))
                .andExpect(jsonPath("$.totals.maxLeadGrade").value("6a"))
                .andExpect(
                        jsonPath("$.routes[*].route.id")
                                .value(
                                        contains(
                                                sixB.id().toString(),
                                                sixA.id().toString(),
                                                gone.id().toString())))
                .andExpect(jsonPath("$.routes[0].best").value("TOP_ROPE_TOPPED"))
                .andExpect(jsonPath("$.routes[0].ascentCount").value(2))
                .andExpect(jsonPath("$.routes[0].toppedCount").value(1))
                .andExpect(jsonPath("$.routes[1].best").value("LEAD_CLEAN"))
                .andExpect(
                        jsonPath("$.routes[1].firstClimbedOn")
                                .value(today.minusMonths(4).toString()))
                .andExpect(jsonPath("$.routes[1].lastClimbedOn").value(today.toString()))
                .andExpect(jsonPath("$.routes[2].route.status").value("DISMOUNTED"))
                .andExpect(jsonPath("$.grades[*].grade").value(contains("6b", "6a")))
                .andExpect(jsonPath("$.grades[0].topRope").value(1))
                .andExpect(jsonPath("$.grades[0].lead").value(0))
                .andExpect(jsonPath("$.grades[1].topRope").value(0))
                .andExpect(jsonPath("$.grades[1].lead").value(2));
    }

    @Test
    void aNewcomerHasAnEmptyLogbook() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        sessions.log(
                members.create(Role.CLIMBER),
                ClubTime.today(clock),
                9,
                clean(route, ClimbMode.LEAD));

        mockMvc.perform(get("/api/v1/me/logbook").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.sessionCount").value(0))
                .andExpect(jsonPath("$.totals.maxGrade").doesNotExist())
                .andExpect(jsonPath("$.grades.length()").value(0))
                .andExpect(jsonPath("$.routes.length()").value(0));
    }
}
