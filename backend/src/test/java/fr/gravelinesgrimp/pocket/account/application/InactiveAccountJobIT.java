package fr.gravelinesgrimp.pocket.account.application;

import static fr.gravelinesgrimp.pocket.MemberFixtures.PASSWORD;
import static fr.gravelinesgrimp.pocket.MemberFixtures.as;
import static fr.gravelinesgrimp.pocket.MemberFixtures.freshAddress;
import static fr.gravelinesgrimp.pocket.MemberFixtures.xsrf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.MemberFixtures;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/** D-119: the daily job on inactive accounts, called directly. */
@IntegrationTest
class InactiveAccountJobIT {

    @Autowired private InactiveAccountJob job;
    @Autowired private MockMvc mockMvc;
    @Autowired private MemberFixtures members;
    @Autowired private MemberRepository memberRepository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private Clock clock;

    @Test
    void aYearWithoutAVisitPutsTheAccountToSleepAndSignInSaysWhy() throws Exception {
        var away = lastSeen(members.create(Role.CLIMBER), yearsAndDaysAgo(1, 1));
        var neverBack = registered(members.create(Role.SETTER), yearsAndDaysAgo(1, 1));
        var around = lastSeen(members.create(Role.CLIMBER), yearsAndDaysAgo(0, 330));

        job.run();

        for (var asleep : new Member[] {away, neverBack}) {
            var reloaded = reload(asleep);
            assertThat(reloaded.status()).isEqualTo(MemberStatus.DISABLED);
            assertThat(reloaded.isDormant()).isTrue();
            assertThat(systemEntries(asleep.id(), "MEMBER_PUT_TO_SLEEP")).isEqualTo(1);
        }
        assertThat(reload(around).isActive()).isTrue();
        login(away)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DORMANT"));
        // The board sees why on the sheet.
        mockMvc.perform(
                        get("/api/v1/admin/members/" + away.id())
                                .with(as(members.create(Role.BOARD))))
                .andExpect(jsonPath("$.member.status").value("DISABLED"))
                .andExpect(jsonPath("$.member.dormantSince").exists());

        // Run again the same day: nothing more happens.
        job.run();
        assertThat(systemEntries(away.id(), "MEMBER_PUT_TO_SLEEP")).isEqualTo(1);
    }

    @Test
    void twoYearsWithoutAVisitDeleteTheAccountLikeADeletionOfOnesOwn() {
        var gone = lastSeen(members.create(Role.CLIMBER), yearsAndDaysAgo(2, 1));
        var asleep = lastSeen(members.create(Role.CLIMBER), yearsAndDaysAgo(1, 300));
        disableAsleep(asleep);

        job.run();

        assertThat(memberRepository.findById(gone.id())).isEmpty();
        assertThat(systemEntries(gone.id(), "MEMBER_DELETED_INACTIVE")).isEqualTo(1);
        assertThat(reload(asleep).isDormant()).isTrue();
    }

    @Test
    void adminsAreNeverPutToSleepNorDeleted() {
        var admin = lastSeen(members.create(Role.ADMIN), yearsAndDaysAgo(3, 0));

        job.run();

        assertThat(reload(admin).isActive()).isTrue();
    }

    @Test
    void anAccountDisabledByTheBoardStaysSoAndIsDeletedAfterTwoYears() {
        var disabled = members.create(Role.CLIMBER);
        disabled.disable();
        disabled = lastSeen(memberRepository.saveAndFlush(disabled), yearsAndDaysAgo(1, 10));
        var longGone = members.create(Role.CLIMBER);
        longGone.disable();
        longGone = lastSeen(memberRepository.saveAndFlush(longGone), yearsAndDaysAgo(2, 10));

        job.run();

        assertThat(reload(disabled).status()).isEqualTo(MemberStatus.DISABLED);
        assertThat(reload(disabled).isDormant()).isFalse();
        assertThat(memberRepository.findById(longGone.id())).isEmpty();
    }

    @Test
    void anAccountTheBoardWakesUpStartsANewYear() throws Exception {
        var board = members.create(Role.BOARD);
        var sleeper = lastSeen(members.create(Role.CLIMBER), yearsAndDaysAgo(1, 20));
        job.run();
        assertThat(reload(sleeper).isDormant()).isTrue();

        mockMvc.perform(
                        put("/api/v1/admin/members/" + sleeper.id() + "/status")
                                .with(as(board))
                                .with(xsrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.dormantSince").doesNotExist());
        job.run();

        var woken = reload(sleeper);
        assertThat(woken.isActive()).isTrue();
        assertThat(woken.reactivatedAt()).isNotNull();
        // The last visit stays the true one: the board sees when they actually came.
        assertThat(woken.lastSeenAt()).isBefore(clock.instant().minusSeconds(3600L * 24 * 365));
        login(sleeper).andExpect(status().isOk());
    }

    private Instant yearsAndDaysAgo(int years, int days) {
        return clock.instant()
                .atZone(ClubTime.ZONE)
                .minusYears(years)
                .minusDays(days)
                .toInstant();
    }

    private Member lastSeen(Member member, Instant at) {
        jdbc.update(
                "update member set created_at = ?, last_seen_at = ? where id = ?",
                Timestamp.from(at.minusSeconds(3600L * 24 * 30)),
                Timestamp.from(at),
                member.id());
        return member;
    }

    private Member registered(Member member, Instant at) {
        jdbc.update(
                "update member set created_at = ?, last_seen_at = null where id = ?",
                Timestamp.from(at),
                member.id());
        return member;
    }

    private void disableAsleep(Member member) {
        jdbc.update(
                "update member set status = 'DISABLED', dormant_since = ? where id = ?",
                Timestamp.from(clock.instant()),
                member.id());
    }

    private Member reload(Member member) {
        return memberRepository.findById(member.id()).orElseThrow();
    }

    private long systemEntries(UUID memberId, String action) {
        return jdbc.queryForObject(
                "select count(*) from audit_entry where target_id = ? and action = ?"
                        + " and actor_id is null and actor_nickname is null",
                Long.class,
                memberId,
                action);
    }

    private ResultActions login(Member member) throws Exception {
        return mockMvc.perform(
                post("/api/v1/auth/login")
                        .with(xsrf())
                        .with(freshAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nickname": "%s", "password": "%s", "rememberMe": false}
                                """
                                        .formatted(member.nickname(), PASSWORD)));
    }
}
