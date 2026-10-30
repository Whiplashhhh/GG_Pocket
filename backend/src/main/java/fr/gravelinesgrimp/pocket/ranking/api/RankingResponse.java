package fr.gravelinesgrimp.pocket.ranking.api;

import fr.gravelinesgrimp.pocket.ranking.application.RankingView;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingStatus;
import fr.gravelinesgrimp.pocket.topo.api.MemberRefResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.List;

@Schema(name = "Ranking")
public record RankingResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "2026-10") String month,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description =
                                "LIVE: this month; PROVISIONAL: last month, sessions may still be"
                                        + " logged; FROZEN: final")
                RankingStatus status,
        @Schema(description = "When the month freezes; absent once frozen") Instant freezesAt,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "False when I chose not to appear in the rankings")
                boolean appearsInRankings,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Climbing days (two sessions on a day make one)")
                Board sessions,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Topped ascents, every mode, repeats included")
                Board ascents) {

    @Schema(name = "RankingBoard")
    public record Board(
            @Schema(
                            requiredMode = RequiredMode.REQUIRED,
                            description = "Best first; ties share a rank, then alphabetical order")
                    List<Line> lines,
            @Schema(description = "My line, absent when I am not ranked") Line me) {

        static Board from(RankingView.Board board) {
            return new Board(
                    board.lines().stream().map(Line::from).toList(),
                    board.me() == null ? null : Line.from(board.me()));
        }
    }

    @Schema(name = "RankingLine")
    public record Line(
            @Schema(requiredMode = RequiredMode.REQUIRED) int rank,
            @Schema(requiredMode = RequiredMode.REQUIRED) MemberRefResponse member,
            @Schema(requiredMode = RequiredMode.REQUIRED) int value) {

        static Line from(RankingView.Line line) {
            return new Line(line.rank(), MemberRefResponse.from(line.member()), line.value());
        }
    }

    @Schema(name = "RankingMonth")
    public record MonthItem(
            @Schema(requiredMode = RequiredMode.REQUIRED, example = "2026-10") String month,
            @Schema(requiredMode = RequiredMode.REQUIRED) RankingStatus status) {

        static MonthItem from(RankingView.Month month) {
            return new MonthItem(month.month().toString(), month.status());
        }
    }

    static RankingResponse from(RankingView view) {
        return new RankingResponse(
                view.month().toString(),
                view.status(),
                view.freezesAt(),
                view.appearsInRankings(),
                Board.from(view.sessions()),
                Board.from(view.ascents()));
    }
}
