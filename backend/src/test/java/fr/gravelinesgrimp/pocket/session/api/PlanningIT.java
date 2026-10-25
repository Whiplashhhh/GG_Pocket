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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

/** The planning: my sessions of a month, the month summary and my streak. */
@IntegrationTest
class PlanningIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private Clock clock;

    @Test
    void aPastMonthShowsItsDaysAndItsSummary() throws Exception {
        var me = members.create(Role.CLIMBER);
        var setter = members.create(Role.SETTER);
        var sixA = routes.create(setter, "6a");
        var sixB = routes.create(setter, "6b");
        var month = YearMonth.from(ClubTime.today(clock)).minusMonths(3);
        sessions.log(
                me, month.atDay(5), 7, topped(sixA, ClimbMode.LEAD), tried(sixB, ClimbMode.LEAD));
        sessions.log(me, month.atDay(5), null, topped(sixA, ClimbMode.TOP_ROPE));
        sessions.log(me, month.atDay(12), 8, clean(sixB, ClimbMode.TOP_ROPE));
        sessions.log(me, month.minusMonths(1).atEndOfMonth(), 2, clean(sixB, ClimbMode.LEAD));
        sessions.log(me, month.plusMonths(1).atDay(1), 2, clean(sixB, ClimbMode.LEAD));

        mockMvc.perform(get("/api/v1/me/planning").param("month", month.toString()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(month.toString()))
                .andExpect(jsonPath("$.frozen").value(true))
                .andExpect(
                        jsonPath("$.days[*].date")
                                .value(
                                        contains(
                                                month.atDay(5).toString(),
                                                month.atDay(12).toString())))
                .andExpect(jsonPath("$.days[0].sessions.length()").value(2))
                .andExpect(jsonPath("$.days[0].sessions[0].feeling").value(7))
                .andExpect(jsonPath("$.days[0].sessions[0].summary.ascentCount").value(2))
                .andExpect(jsonPath("$.days[0].sessions[0].summary.toppedCount").value(1))
                .andExpect(jsonPath("$.days[0].sessions[1].feeling").doesNotExist())
                .andExpect(jsonPath("$.days[1].sessions[0].summary.maxGrade").value("6b"))
                .andExpect(jsonPath("$.totals.climbingDays").value(2))
                .andExpect(jsonPath("$.totals.sessionCount").value(3))
                .andExpect(jsonPath("$.totals.toppedCount").value(3))
                .andExpect(jsonPath("$.totals.maxGrade").value("6b"))
                .andExpect(jsonPath("$.totals.averageFeeling").value(7.5));
    }

    @Test
    void anEmptyMonthHasNoSummaryFigures() throws Exception {
        var me = members.create(Role.CLIMBER);
        var month = YearMonth.from(ClubTime.today(clock)).minusMonths(5);

        mockMvc.perform(get("/api/v1/me/planning").param("month", month.toString()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days.length()").value(0))
                .andExpect(jsonPath("$.totals.climbingDays").value(0))
                .andExpect(jsonPath("$.totals.maxGrade").doesNotExist())
                .andExpect(jsonPath("$.totals.averageFeeling").doesNotExist())
                .andExpect(jsonPath("$.streak.weeks").value(0));
    }

    @Test
    void thisMonthByDefaultWithMyStreak() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var today = ClubTime.today(clock);
        var monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // Two days in each of the last two weeks, none yet this week.
        for (var day :
                new java.time.LocalDate[] {
                    monday.minusDays(7),
                    monday.minusDays(6),
                    monday.minusDays(14),
                    monday.minusDays(12)
                }) {
            sessions.log(me, day, null, topped(route, ClimbMode.TOP_ROPE));
        }

        mockMvc.perform(get("/api/v1/me/planning").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(YearMonth.from(today).toString()))
                .andExpect(jsonPath("$.frozen").value(false))
                .andExpect(jsonPath("$.streak.weeks").value(2))
                .andExpect(jsonPath("$.streak.minDaysPerWeek").value(2))
                .andExpect(jsonPath("$.streak.currentWeekDays").value(0));
    }

    @Test
    void iNeverSeeTheSessionsOfSomeoneElse() throws Exception {
        var me = members.create(Role.CLIMBER);
        var someoneElse = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var month = YearMonth.from(ClubTime.today(clock)).minusMonths(2);
        sessions.log(someoneElse, month.atDay(3), 9, clean(route, ClimbMode.LEAD));

        mockMvc.perform(get("/api/v1/me/planning").param("month", month.toString()).with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days.length()").value(0))
                .andExpect(jsonPath("$.totals.sessionCount").value(0));
    }

    @Test
    void refusesAFutureOrMalformedMonth() throws Exception {
        var me = members.create(Role.CLIMBER);
        var next = YearMonth.from(ClubTime.today(clock)).plusMonths(1);
        for (var month : new String[] {next.toString(), "2026-13", "octobre"}) {
            mockMvc.perform(get("/api/v1/me/planning").param("month", month).with(as(me)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }
}
