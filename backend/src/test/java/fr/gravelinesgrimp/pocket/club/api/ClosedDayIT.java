package fr.gravelinesgrimp.pocket.club.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.badge.domain.FreeSessions;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.session.domain.AscentDraft;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionDraft;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * The days the gym is closed (D-116): the board and the admin set them, every planning shows them,
 * and the "Mois complet" badge follows. Each test works in its own year, far in the past, as the
 * database is shared.
 */
@IntegrationTest
class ClosedDayIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private ClimbingSessionRepository sessions;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void theBoardClosesARangeAndReopensADay() throws Exception {
        var board = members.create(Role.BOARD);

        close(board, "2003-12-22", "2003-12-24", "  Vacances de Noël ")
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].date")
                                .value(hasItems("2003-12-22", "2003-12-23", "2003-12-24")))
                .andExpect(
                        jsonPath("$[?(@.date == '2003-12-23')].label")
                                .value(hasItem("Vacances de Noël")));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(board)))
                .andExpect(jsonPath("$.entries[0].action").value("CLUB_DAYS_CLOSED"))
                .andExpect(jsonPath("$.entries[0].targetType").value("CLOSED_DAY"))
                .andExpect(jsonPath("$.entries[0].targetLabel").value("2003-12-22 / 2003-12-24"));

        mockMvc.perform(
                        delete("/api/v1/admin/closed-days")
                                .param("from", "2003-12-23")
                                .with(as(board))
                                .with(xsrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/closed-days").with(as(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].date").value(hasItems("2003-12-22", "2003-12-24")))
                .andExpect(jsonPath("$[*].date").value(not(hasItem("2003-12-23"))));
        mockMvc.perform(get("/api/v1/admin/audit").with(as(board)))
                .andExpect(jsonPath("$.entries[0].action").value("CLUB_DAYS_REOPENED"));
    }

    @Test
    void closingAgainOnlyChangesTheLabel() throws Exception {
        var admin = members.create(Role.ADMIN);
        close(admin, "2004-05-01", null, null).andExpect(status().isOk());

        close(admin, "2004-05-01", null, "Fête du travail")
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.date == '2004-05-01')].label")
                                .value(hasItem("Fête du travail")));
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from closed_day where closed_on = ?",
                                Integer.class,
                                LocalDate.parse("2004-05-01")))
                .isOne();
    }

    @Test
    void aRangeMustGoForwardAndStayUnderAYear() throws Exception {
        var board = members.create(Role.BOARD);
        close(board, "2005-06-02", "2005-06-01", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        close(board, "2005-01-01", "2006-01-02", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        close(board, "2005-01-01", null, "x".repeat(61))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void onlyTheBoardAndTheAdminSeeAndChangeThem() throws Exception {
        for (var role : List.of(Role.TOURIST, Role.CLIMBER, Role.SETTER)) {
            var member = members.create(role);
            mockMvc.perform(get("/api/v1/admin/closed-days").with(as(member)))
                    .andExpect(status().isForbidden());
            close(member, "2007-02-01", null, null).andExpect(status().isForbidden());
            mockMvc.perform(
                            delete("/api/v1/admin/closed-days")
                                    .param("from", "2007-02-01")
                                    .with(as(member))
                                    .with(xsrf()))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(get("/api/v1/admin/closed-days")).andExpect(status().isUnauthorized());
        mockMvc.perform(
                        post("/api/v1/admin/closed-days")
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"from\": \"2007-02-01\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(
                        jdbc.queryForObject(
                                "select count(*) from closed_day where closed_on = ?",
                                Integer.class,
                                LocalDate.parse("2007-02-01")))
                .isZero();
    }

    @Test
    void everyPlanningShowsTheClosedDaysOfItsMonth() throws Exception {
        close(members.create(Role.BOARD), "2008-02-14", null, "Saint-Valentin au resto")
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/v1/me/planning")
                                .param("month", "2008-02")
                                .with(as(members.create(Role.CLIMBER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closedDays[0].date").value("2008-02-14"))
                .andExpect(jsonPath("$.closedDays[0].label").value("Saint-Valentin au resto"));
        mockMvc.perform(
                        get("/api/v1/me/planning")
                                .param("month", "2008-03")
                                .with(as(members.create(Role.TOURIST))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closedDays").isEmpty());
    }

    @Test
    void closingTheOnlyMissedDayCompletesTheMonthAndReopeningItTakesItBack() throws Exception {
        var climber = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var month = YearMonth.of(2009, 3);
        var freeDays =
                month.atDay(1)
                        .datesUntil(month.atEndOfMonth().plusDays(1))
                        .filter(day -> FreeSessions.WEEKDAYS.contains(day.getDayOfWeek()))
                        .toList();
        var missed = freeDays.getFirst();
        freeDays.stream().skip(1).forEach(day -> climb(climber, route, day));
        var board = members.create(Role.BOARD);

        close(board, missed.toString(), null, null).andExpect(status().isOk());
        assertThat(monthsComplete(climber)).isOne();

        mockMvc.perform(
                        delete("/api/v1/admin/closed-days")
                                .param("from", missed.toString())
                                .param("to", missed.toString())
                                .with(as(board))
                                .with(xsrf()))
                .andExpect(status().isNoContent());
        assertThat(monthsComplete(climber)).isZero();
    }

    private ResultActions close(Member actor, String from, String to, String label)
            throws Exception {
        var body =
                "{\"from\": \"%s\"".formatted(from)
                        + (to == null ? "" : ", \"to\": \"%s\"".formatted(to))
                        + (label == null ? "" : ", \"label\": \"%s\"".formatted(label))
                        + "}";
        return mockMvc.perform(
                post("/api/v1/admin/closed-days")
                        .with(as(actor))
                        .with(xsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    private void climb(Member climber, Route route, LocalDate day) {
        var draft =
                new SessionDraft(
                        day,
                        null,
                        null,
                        SessionVisibility.FRIENDS,
                        List.of(
                                new AscentDraft(
                                        route.id(), ClimbMode.LEAD, true, true, 0, true, null)));
        sessions.saveAndFlush(ClimbingSession.create(climber.id(), UUID.randomUUID(), draft));
    }

    private Integer monthsComplete(Member climber) {
        return jdbc.queryForObject(
                "select count(*) from badge_award where member_id = ? and code = 'MONTH_COMPLETE'",
                Integer.class,
                climber.id());
    }
}
