package fr.gravelinesgrimp.pocket.ranking.api;

import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;
import static fr.gravelinesgrimp.pocket.SessionFixtures.clean;
import static fr.gravelinesgrimp.pocket.SessionFixtures.topped;
import static fr.gravelinesgrimp.pocket.SessionFixtures.tried;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.RouteFixtures;
import fr.gravelinesgrimp.pocket.SessionFixtures;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberDetails;
import fr.gravelinesgrimp.pocket.member.domain.MemberIdentity;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.ranking.application.RankingClosure;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The podium. The database is shared by every integration test, so the tests on exact ranks use a
 * month before anything logged or frozen so far: nobody else climbed in it, and no closure froze
 * it.
 */
@IntegrationTest
class RankingIT {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private RouteFixtures routes;
    @Autowired private SessionFixtures sessions;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RankingClosure closure;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;

    @Test
    void tiesShareARankThenFollowTheAlphabet() throws Exception {
        var month = untouchedMonth();
        var route = routes.create(members.create(Role.SETTER));
        var zazie = climber("Zazie");
        var abel = climber("abel");
        var bruno = climber("bruno");
        var hidden = climber("hidden");
        var gone = climber("gone");
        // Days: Zazie and abel 2 each, bruno 1 (two sessions that day). Topped: bruno 5, the others
        // 2.
        sessions.log(
                abel,
                month.atDay(3),
                null,
                topped(route, ClimbMode.LEAD),
                tried(route, ClimbMode.LEAD));
        sessions.log(abel, month.atDay(10), null, topped(route, ClimbMode.TOP_ROPE));
        sessions.log(zazie, month.atDay(4), null, clean(route, ClimbMode.LEAD));
        sessions.log(
                zazie,
                month.atDay(11),
                null,
                tried(route, ClimbMode.LEAD),
                topped(route, ClimbMode.LEAD));
        sessions.log(
                bruno,
                month.atDay(5),
                null,
                topped(route, ClimbMode.LEAD),
                topped(route, ClimbMode.LEAD),
                topped(route, ClimbMode.LEAD));
        sessions.log(
                bruno,
                month.atDay(5),
                null,
                topped(route, ClimbMode.LEAD),
                topped(route, ClimbMode.TOP_ROPE));
        // Out of the rankings: they would lead both.
        for (var member : new Member[] {hidden, gone}) {
            for (var day = 1; day <= 6; day++) {
                sessions.log(member, month.atDay(day), null, topped(route, ClimbMode.LEAD));
            }
        }
        hidden.describe(new MemberDetails(null, null, null, false, false));
        memberRepository.saveAndFlush(hidden);
        gone.disable();
        memberRepository.saveAndFlush(gone);

        mockMvc.perform(get("/api/v1/rankings").param("month", month.toString()).with(as(abel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(month.toString()))
                .andExpect(jsonPath("$.status").value("FROZEN"))
                .andExpect(jsonPath("$.freezesAt").doesNotExist())
                .andExpect(jsonPath("$.appearsInRankings").value(true))
                .andExpect(
                        jsonPath("$.sessions.lines[*].member.nickname")
                                .value(
                                        contains(
                                                abel.nickname(),
                                                zazie.nickname(),
                                                bruno.nickname())))
                .andExpect(jsonPath("$.sessions.lines[*].rank").value(contains(1, 1, 3)))
                .andExpect(jsonPath("$.sessions.lines[*].value").value(contains(2, 2, 1)))
                .andExpect(jsonPath("$.sessions.me.rank").value(1))
                .andExpect(jsonPath("$.sessions.me.value").value(2))
                .andExpect(
                        jsonPath("$.ascents.lines[*].member.nickname")
                                .value(
                                        contains(
                                                bruno.nickname(),
                                                abel.nickname(),
                                                zazie.nickname())))
                .andExpect(jsonPath("$.ascents.lines[*].rank").value(contains(1, 2, 2)))
                .andExpect(jsonPath("$.ascents.lines[*].value").value(contains(5, 2, 2)))
                .andExpect(jsonPath("$.ascents.me.rank").value(2));

        mockMvc.perform(get("/api/v1/rankings").param("month", month.toString()).with(as(hidden)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appearsInRankings").value(false))
                .andExpect(jsonPath("$.sessions.lines.length()").value(3))
                .andExpect(jsonPath("$.sessions.me").doesNotExist())
                .andExpect(jsonPath("$.ascents.me").doesNotExist());
    }

    @Test
    void theClosureFreezesAMonthOnceAndKeepsTheRanksOfDeletedAccounts() throws Exception {
        var month = untouchedMonth();
        var route = routes.create(members.create(Role.SETTER));
        var first = climber("first");
        var second = climber("second");
        var reader = members.create(Role.CLIMBER);
        sessions.log(first, month.atDay(2), null, topped(route, ClimbMode.LEAD));
        sessions.log(first, month.atDay(9), null, topped(route, ClimbMode.LEAD));
        sessions.log(second, month.atDay(9), null, topped(route, ClimbMode.LEAD));

        assertThat(closure.closeDueMonths(clock.instant())).contains(month);
        assertThat(closure.closeDueMonths(clock.instant())).isEmpty();

        // Frozen: what is logged afterwards (only possible behind the rules) changes nothing.
        sessions.log(second, month.atDay(20), null, topped(route, ClimbMode.LEAD));
        sessions.log(second, month.atDay(21), null, topped(route, ClimbMode.LEAD));
        mockMvc.perform(get("/api/v1/rankings").param("month", month.toString()).with(as(second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"))
                .andExpect(
                        jsonPath("$.sessions.lines[*].member.nickname")
                                .value(contains(first.nickname(), second.nickname())))
                .andExpect(jsonPath("$.sessions.lines[*].rank").value(contains(1, 2)))
                .andExpect(jsonPath("$.sessions.me.value").value(1))
                .andExpect(jsonPath("$.ascents.lines[*].rank").value(contains(1, 2)));

        // Leaving the rankings, like deleting the account, keeps the frozen rank, held by
        // "Ancien·ne membre": nobody moves.
        second.describe(new MemberDetails(null, null, null, false, false));
        memberRepository.saveAndFlush(second);
        mockMvc.perform(
                        delete("/api/v1/admin/members/" + first.id())
                                .with(as(members.create(Role.ADMIN)))
                                .with(xsrf()))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/rankings").param("month", month.toString()).with(as(reader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions.lines[*].rank").value(contains(1, 2)))
                .andExpect(jsonPath("$.sessions.lines[*].value").value(contains(2, 1)))
                .andExpect(
                        jsonPath("$.sessions.lines[*].member.id")
                                .value(
                                        contains(
                                                FormerMember.ID.toString(),
                                                FormerMember.ID.toString())))
                .andExpect(
                        jsonPath("$.sessions.lines[*].member.nickname")
                                .value(contains("Ancien·ne membre", "Ancien·ne membre")))
                .andExpect(jsonPath("$.sessions.lines[1].member.avatarUrl").doesNotExist())
                .andExpect(jsonPath("$.sessions.me").doesNotExist());

        mockMvc.perform(get("/api/v1/rankings").param("month", month.toString()).with(as(second)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appearsInRankings").value(false))
                .andExpect(jsonPath("$.sessions.me").doesNotExist());

        mockMvc.perform(get("/api/v1/me/export").with(as(second)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.frozenRanks[*].month")
                                .value(contains(month.toString(), month.toString())))
                .andExpect(
                        jsonPath("$.frozenRanks[*].ranking").value(contains("ASCENTS", "SESSIONS")))
                .andExpect(jsonPath("$.frozenRanks[*].rank").value(contains(2, 2)));
    }

    @Test
    void thisMonthIsLiveByDefault() throws Exception {
        var me = members.create(Role.CLIMBER);
        var route = routes.create(members.create(Role.SETTER));
        var today = ClubTime.today(clock);
        sessions.log(
                me, today, null, topped(route, ClimbMode.LEAD), clean(route, ClimbMode.TOP_ROPE));
        var current = YearMonth.from(today);
        sessions.log(me, current.minusMonths(3).atDay(1), null, topped(route, ClimbMode.LEAD));

        mockMvc.perform(get("/api/v1/rankings").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(current.toString()))
                .andExpect(jsonPath("$.status").value("LIVE"))
                .andExpect(
                        jsonPath("$.freezesAt")
                                .value(SessionCalendar.freezesAt(current).toString()))
                .andExpect(jsonPath("$.sessions.me.value").value(1))
                .andExpect(jsonPath("$.ascents.me.value").value(2));

        mockMvc.perform(get("/api/v1/rankings/months").with(as(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value(current.toString()))
                .andExpect(jsonPath("$[0].status").value("LIVE"))
                .andExpect(jsonPath("$[2].status").value("FROZEN"));
    }

    @Test
    void aFutureOrMalformedMonthIsRefused() throws Exception {
        var me = members.create(Role.CLIMBER);
        var next = YearMonth.from(ClubTime.today(clock)).plusMonths(1);

        for (var month : new String[] {next.toString(), "2026-13", "octobre"}) {
            mockMvc.perform(get("/api/v1/rankings").param("month", month).with(as(me)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
    }

    /** A climber whose nickname starts with {@code prefix}, still unique in the shared database. */
    private Member climber(String prefix) {
        var member = members.create(Role.CLIMBER);
        member.rename(
                new MemberIdentity(
                        prefix
                                + SEQUENCE.incrementAndGet()
                                + "r"
                                + member.id().toString().substring(0, 4),
                        member.firstName(),
                        member.lastName(),
                        null));
        return memberRepository.saveAndFlush(member);
    }

    /**
     * A month before every session and every frozen month so far (at least two months back, so
     * frozen by date): nobody else's sessions are in it, and no closure froze it yet.
     */
    private YearMonth untouchedMonth() {
        var earliest =
                jdbc.queryForObject(
                        "select least((select min(climbed_on) from climbing_session),"
                                + " (select min(month) from monthly_ranking_closure))",
                        LocalDate.class);
        var twoMonthsBack = YearMonth.from(ClubTime.today(clock)).minusMonths(2);
        if (earliest == null) {
            return twoMonthsBack;
        }
        var before = YearMonth.from(earliest).minusMonths(1);
        return before.isBefore(twoMonthsBack) ? before : twoMonthsBack;
    }
}
