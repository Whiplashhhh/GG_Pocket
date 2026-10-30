package fr.gravelinesgrimp.pocket.ranking.application;

import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.ranking.domain.RankedScore;
import fr.gravelinesgrimp.pocket.ranking.domain.Ranking;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingKind;
import fr.gravelinesgrimp.pocket.ranking.infra.MonthlyRankingRepository;
import fr.gravelinesgrimp.pocket.ranking.infra.ScoreRow;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/** A month's rankings counted from its sessions: what the closure freezes. */
@Component
class LiveRankings {

    private final MonthlyRankingRepository rankings;

    LiveRankings(MonthlyRankingRepository rankings) {
        this.rankings = rankings;
    }

    List<RankedScore> of(RankingKind kind, YearMonth month) {
        var from = month.atDay(1);
        var to = month.atEndOfMonth();
        var rows =
                switch (kind) {
                    case SESSIONS -> rankings.countClimbingDays(from, to, MemberStatus.ACTIVE);
                    case ASCENTS -> rankings.countToppedAscents(from, to, MemberStatus.ACTIVE);
                };
        return Ranking.of(rows.stream().map(ScoreRow::toScore).toList());
    }
}
