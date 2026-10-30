package fr.gravelinesgrimp.pocket.ranking.application;

import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.ranking.domain.RankedScore;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingKind;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingStatus;
import fr.gravelinesgrimp.pocket.ranking.infra.FrozenRow;
import fr.gravelinesgrimp.pocket.ranking.infra.MonthlyRankingRepository;
import fr.gravelinesgrimp.pocket.session.domain.PlanningMonth;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The podium (spec §8.7, §9.2): any member reads the rankings of every month. The months not frozen
 * yet are counted live; a frozen month shows its frozen ranks, without the members who have since
 * been disabled or left the rankings (their ranks are kept, D-103).
 */
@Service
public class RankingService {

    private final MonthlyRankingRepository rankings;
    private final LiveRankings live;
    private final MemberRepository members;
    private final Clock clock;

    RankingService(
            MonthlyRankingRepository rankings,
            LiveRankings live,
            MemberRepository members,
            Clock clock) {
        this.rankings = rankings;
        this.live = live;
        this.members = members;
        this.clock = clock;
    }

    /** The months that have rankings, from this one back to the first climbed one. */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public List<RankingView.Month> months() {
        var now = clock.instant();
        var today = ClubTime.today(clock);
        var current = YearMonth.from(today);
        var first =
                rankings.findFirstClimbingDay()
                        .map(YearMonth::from)
                        .filter(month -> month.isBefore(current))
                        .orElse(current);
        var months = new ArrayList<RankingView.Month>();
        for (var month = current; !month.isBefore(first); month = month.minusMonths(1)) {
            months.add(new RankingView.Month(month, RankingStatus.of(month, now, today)));
        }
        return months;
    }

    /**
     * @param month {@code YYYY-MM}, or null for this month
     * @throws fr.gravelinesgrimp.pocket.platform.domain.BusinessException {@code VALIDATION_FAILED}
     *     for a malformed or future month
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public RankingView of(UUID memberId, String month) {
        var now = clock.instant();
        var today = ClubTime.today(clock);
        var shown = PlanningMonth.parse(month, today);
        var appears = members.findById(memberId).map(Member::appearsInRankings).orElse(false);
        if (rankings.isClosed(shown.atDay(1))) {
            var rows = rankings.findShown(shown.atDay(1), FormerMember.ID, MemberStatus.ACTIVE);
            return new RankingView(
                    shown,
                    RankingStatus.FROZEN,
                    null,
                    appears,
                    RankingView.Board.of(frozen(rows, RankingKind.SESSIONS), memberId),
                    RankingView.Board.of(frozen(rows, RankingKind.ASCENTS), memberId));
        }
        var status = RankingStatus.of(shown, now, today);
        return new RankingView(
                shown,
                status,
                status == RankingStatus.FROZEN ? null : SessionCalendar.freezesAt(shown),
                appears,
                RankingView.Board.of(live.of(RankingKind.SESSIONS, shown), memberId),
                RankingView.Board.of(live.of(RankingKind.ASCENTS, shown), memberId));
    }

    /** The frozen ranks of one ranking, ties in the alphabetical order of today's nicknames. */
    private static List<RankedScore> frozen(List<FrozenRow> rows, RankingKind kind) {
        var collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.SECONDARY);
        return rows.stream()
                .filter(row -> row.kind() == kind)
                .map(FrozenRow::toRankedScore)
                .sorted(
                        Comparator.comparingInt(RankedScore::rank)
                                .thenComparing(ranked -> ranked.score().nickname(), collator))
                .toList();
    }
}
