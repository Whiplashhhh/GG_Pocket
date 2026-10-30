package fr.gravelinesgrimp.pocket.stats.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;
import static fr.gravelinesgrimp.pocket.SessionFixtures.topped;
import static fr.gravelinesgrimp.pocket.SessionFixtures.tried;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * The statistics of the management space. The database is shared by every integration test: exact
 * totals use a month before anything logged so far, and route lines are found by route id.
 */
@IntegrationTest
class StatsIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;

    @Test
    void theAttendanceOfAMonthCountsEveryAttemptRouteByRoute() throws Exception {
        var month = untouchedMonth();
        var setter = members.create(Role.SETTER);
        var busy = routes.create(setter);
        var quiet = routes.create(setter);
        var anna = members.create(Role.CLIMBER);
        var ben = members.create(Role.CLIMBER);
        sessions.log(
                anna,
                month.atDay(2),
                null,
                topped(busy, ClimbMode.LEAD),
                tried(busy, ClimbMode.LEAD));
        // "Juste moi" sessions count too: only totals come out.
        sessions.log(
                ben,
                month.atDay(9),
                null,
                SessionVisibility.PRIVATE,
                clean(busy, ClimbMode.TOP_ROPE),
                tried(quiet, ClimbMode.LEAD));
        // Next month: not counted.
        sessions.log(anna, month.plusMonths(1).atDay(1), null, topped(quiet, ClimbMode.LEAD));

        var body =
                mockMvc.perform(
                                get("/api/v1/stats/attendance")
                                        .param("month", month.toString())
                                        .with(as(setter)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.month").value(month.toString()))
                        .andExpect(jsonPath("$.total.ascentCount").value(4))
                        .andExpect(jsonPath("$.total.toppedCount").value(2))
                        .andExpect(jsonPath("$.total.cleanCount").value(1))
                        .andExpect(jsonPath("$.total.climberCount").value(2))
                        .andExpect(jsonPath("$.total.successPercent").value(50))
                        .andExpect(line(busy.id(), "ascentCount").value(contains(3)))
                        .andExpect(line(busy.id(), "toppedCount").value(contains(2)))
                        .andExpect(line(busy.id(), "cleanCount").value(contains(1)))
                        .andExpect(line(busy.id(), "climberCount").value(contains(2)))
                        .andExpect(line(busy.id(), "successPercent").value(contains(67)))
                        .andExpect(line(quiet.id(), "ascentCount").value(contains(1)))
                        .andExpect(line(quiet.id(), "successPercent").value(contains(0)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        // Most climbed first.
        List<String> ids = JsonPath.read(body, "$.routes[*].route.id");
        assertThat(ids.indexOf(busy.id().toString()), lessThan(ids.indexOf(quiet.id().toString())));
    }

    @Test
    void thisMonthListsTheRoutesOnTheWallNobodyClimbed() throws Exception {
        var setter = members.create(Role.SETTER);
        var lonely = routes.create(setter);

        mockMvc.perform(get("/api/v1/stats/attendance").with(as(setter)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.month").value(YearMonth.from(ClubTime.today(clock)).toString()))
                .andExpect(line(lonely.id(), "ascentCount").value(contains(0)))
                .andExpect(
                        jsonPath(
                                        "$.routes[?(@.route.id == '%s')].attendance.successPercent",
                                        lonely.id())
                                .value(empty()));
    }

    @Test
    void theRoutesOnTheWallCountTheirAscentsSinceTheyOpened() throws Exception {
        var setter = members.create(Role.SETTER);
        var active = routes.create(setter);
        var lastChance = routes.create(setter, RouteStatus.LAST_CHANCE);
        var dismounted = routes.create(setter, RouteStatus.DISMOUNTED);
        var climber = members.create(Role.CLIMBER);
        var today = ClubTime.today(clock);
        sessions.log(climber, today.minusDays(40), null, topped(active, ClimbMode.LEAD));
        sessions.log(
                climber,
                today,
                null,
                tried(active, ClimbMode.LEAD),
                topped(lastChance, ClimbMode.TOP_ROPE));

        mockMvc.perform(get("/api/v1/stats/routes").with(as(setter)))
                .andExpect(status().isOk())
                .andExpect(wallLine(active.id(), "ascentCount").value(contains(2)))
                .andExpect(wallLine(active.id(), "toppedCount").value(contains(1)))
                .andExpect(wallLine(active.id(), "climberCount").value(contains(1)))
                .andExpect(wallLine(lastChance.id(), "successPercent").value(contains(100)))
                .andExpect(jsonPath("$[?(@.route.id == '%s')]", dismounted.id()).value(empty()));
    }

    @Test
    void climbersCannotReadTheStatistics() throws Exception {
        var climber = members.create(Role.CLIMBER);

        for (var path : new String[] {"/api/v1/stats/attendance", "/api/v1/stats/routes"}) {
            mockMvc.perform(get(path).with(as(climber))).andExpect(status().isForbidden());
        }
    }

    @Test
    void aFutureOrMalformedMonthIsRefused() throws Exception {
        var setter = members.create(Role.SETTER);
        var next = YearMonth.from(ClubTime.today(clock)).plusMonths(1);

        for (var month : new String[] {next.toString(), "2026-13", "octobre"}) {
            mockMvc.perform(get("/api/v1/stats/attendance").param("month", month).with(as(setter)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }

    private static JsonPathResultMatchers line(UUID routeId, String field) {
        return jsonPath("$.routes[?(@.route.id == '%s')].attendance." + field, routeId);
    }

    private static JsonPathResultMatchers wallLine(UUID routeId, String field) {
        return jsonPath("$[?(@.route.id == '%s')].attendance." + field, routeId);
    }

    /** A month before anything logged: nobody else climbed in it. */
    private YearMonth untouchedMonth() {
        var earliest =
                jdbc.queryForObject(
                        "select min(climbed_on) from climbing_session", LocalDate.class);
        var twoMonthsBack = YearMonth.from(ClubTime.today(clock)).minusMonths(2);
        if (earliest == null) {
            return twoMonthsBack;
        }
        var before = YearMonth.from(earliest).minusMonths(1);
        return before.isBefore(twoMonthsBack) ? before : twoMonthsBack;
    }
}
