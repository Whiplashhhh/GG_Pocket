package fr.gravelinesgrimp.pocket.ranking.application;

import fr.gravelinesgrimp.pocket.ranking.domain.RankedScore;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingStatus;
import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * A month of the podium: both rankings, and where I stand.
 *
 * @param freezesAt when the month freezes; null once frozen
 * @param appearsInRankings false when I chose not to appear (then I am in no line)
 */
public record RankingView(
        YearMonth month,
        RankingStatus status,
        Instant freezesAt,
        boolean appearsInRankings,
        Board sessions,
        Board ascents) {

    /**
     * @param me my line, or null when I am not ranked
     */
    public record Board(List<Line> lines, Line me) {

        static Board of(List<RankedScore> ranked, UUID memberId) {
            var lines = ranked.stream().map(Line::of).toList();
            var me = lines.stream().filter(line -> line.member().id().equals(memberId)).findFirst();
            return new Board(lines, me.orElse(null));
        }
    }

    public record Line(int rank, MemberRefView member, int value) {

        static Line of(RankedScore ranked) {
            var score = ranked.score();
            return new Line(
                    ranked.rank(),
                    new MemberRefView(score.memberId(), score.nickname(), score.avatarId()),
                    score.value());
        }
    }

    public record Month(YearMonth month, RankingStatus status) {}
}
