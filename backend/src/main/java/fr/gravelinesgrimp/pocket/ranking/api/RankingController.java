package fr.gravelinesgrimp.pocket.ranking.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.ranking.application.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The podium: the monthly rankings (spec §8.7). */
@RestController
@RequestMapping(path = "/api/v1/rankings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Rankings")
class RankingController {

    private final RankingService rankings;

    RankingController(RankingService rankings) {
        this.rankings = rankings;
    }

    @GetMapping
    @Operation(
            operationId = "getRanking",
            summary = "One month of the podium: the sessions and routes rankings, and my lines")
    RankingResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Parameter(description = "YYYY-MM, this month by default; never a future month")
                    @RequestParam(required = false)
                    String month) {
        return RankingResponse.from(rankings.of(me.memberId(), month));
    }

    @GetMapping("/months")
    @Operation(
            operationId = "listRankingMonths",
            summary = "The months that have rankings, this one first")
    List<RankingResponse.MonthItem> months() {
        return rankings.months().stream().map(RankingResponse.MonthItem::from).toList();
    }
}
