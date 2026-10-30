package fr.gravelinesgrimp.pocket.ranking.application;

import fr.gravelinesgrimp.pocket.ranking.domain.MonthlyRankingSnapshot;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingKind;
import fr.gravelinesgrimp.pocket.ranking.infra.MonthlyRankingRepository;
import fr.gravelinesgrimp.pocket.session.domain.SessionCalendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * The monthly closure (spec §9.2): freezes the rankings of every month whose sessions can no longer
 * change (D-084) and that is not frozen yet. Idempotent: a month is frozen once, whoever runs it
 * and however often; a missed run is caught up by the next one.
 */
@Service
public class RankingClosure {

    private final MonthlyRankingRepository rankings;
    private final LiveRankings live;

    RankingClosure(MonthlyRankingRepository rankings, LiveRankings live) {
        this.rankings = rankings;
        this.live = live;
    }

    /**
     * @return the months this call froze, oldest first
     */
    @Transactional
    public List<YearMonth> closeDueMonths(Instant now) {
        var first = rankings.findFirstClimbingDay();
        if (first.isEmpty()) {
            return List.of();
        }
        var closed = new ArrayList<YearMonth>();
        for (var month = YearMonth.from(first.get());
                SessionCalendar.isFrozen(month.atDay(1), now);
                month = month.plusMonths(1)) {
            if (rankings.isClosed(month.atDay(1)) || rankings.close(month.atDay(1), now) == 0) {
                continue;
            }
            for (var kind : RankingKind.values()) {
                for (var line : live.of(kind, month)) {
                    rankings.save(new MonthlyRankingSnapshot(month, kind, line));
                }
            }
            closed.add(month);
        }
        return closed;
    }
}
