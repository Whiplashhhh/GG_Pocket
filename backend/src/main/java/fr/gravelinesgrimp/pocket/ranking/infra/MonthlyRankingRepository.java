package fr.gravelinesgrimp.pocket.ranking.infra;

import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.ranking.domain.MonthlyRankingSnapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The monthly rankings (spec §9.2): live scores counted from the sessions, and the frozen months.
 * Live scores only count the active, validated members who appear in the rankings (D-107).
 */
public interface MonthlyRankingRepository extends JpaRepository<MonthlyRankingSnapshot, UUID> {

    /** Climbing days between two days (D-004): two sessions on the same day count once. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.ranking.infra.ScoreRow("
                    + "m.id, m.nickname, m.avatarId, count(distinct s.climbedOn))"
                    + " from ClimbingSession s join Member m on m.id = s.memberId"
                    + " where s.climbedOn between :from and :to"
                    + " and m.status = :active and m.validatedAt is not null"
                    + " and m.appearsInRankings = true"
                    + " group by m.id, m.nickname, m.avatarId")
    List<ScoreRow> countClimbingDays(LocalDate from, LocalDate to, MemberStatus active);

    /** Topped ascents between two days (D-003): every mode, repeats included. */
    @Query(
            "select new fr.gravelinesgrimp.pocket.ranking.infra.ScoreRow("
                    + "m.id, m.nickname, m.avatarId, count(a))"
                    + " from ClimbingSession s join s.ascents a join Member m on m.id = s.memberId"
                    + " where s.climbedOn between :from and :to and a.topped = true"
                    + " and m.status = :active and m.validatedAt is not null"
                    + " and m.appearsInRankings = true"
                    + " group by m.id, m.nickname, m.avatarId")
    List<ScoreRow> countToppedAscents(LocalDate from, LocalDate to, MemberStatus active);

    /** The first day anyone climbed: the rankings go back to its month. */
    @Query("select min(s.climbedOn) from ClimbingSession s")
    Optional<LocalDate> findFirstClimbingDay();

    @Query(
            value = "select exists (select 1 from monthly_ranking_closure where month = :month)",
            nativeQuery = true)
    boolean isClosed(LocalDate month);

    /**
     * Marks a month as frozen, unless it already is.
     *
     * @return 1 when this call froze it, 0 when it was already (the job is idempotent)
     */
    @Modifying
    @Query(
            value =
                    "insert into monthly_ranking_closure (month, closed_at) values (:month, :now)"
                            + " on conflict (month) do nothing",
            nativeQuery = true)
    int close(LocalDate month, Instant now);

    /** The ranks of a frozen month, with who holds them today (D-103). */
    @Query(
            "select new fr.gravelinesgrimp.pocket.ranking.infra.FrozenRow("
                    + "r.kind, r.rank, r.value, m.id, m.nickname, m.avatarId, m.status,"
                    + " m.appearsInRankings)"
                    + " from MonthlyRankingSnapshot r join Member m on m.id = r.memberId"
                    + " where r.month = :month order by r.kind, r.rank")
    List<FrozenRow> findFrozen(LocalDate month);

    /** My frozen ranks, oldest first (my data export). */
    List<MonthlyRankingSnapshot> findAllByMemberIdOrderByMonthAscKindAsc(UUID memberId);

    /** Account deletion (spec §14): the frozen ranks stay, held by "Ancien·ne membre". */
    @Modifying
    @Query(
            value =
                    "update monthly_ranking_snapshot set member_id = :heirId, updated_at = :now,"
                            + " version = version + 1 where member_id = :memberId",
            nativeQuery = true)
    int handOver(UUID memberId, UUID heirId, Instant now);
}
