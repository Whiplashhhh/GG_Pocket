package fr.gravelinesgrimp.pocket.stats.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.stats.application.StatsService;

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

/** The statistics of the management space (spec §8.10): attendance and the topo overview. */
@RestController
@RequestMapping(path = "/api/v1/stats", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Stats")
class StatsController {

    private final StatsService stats;

    StatsController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping("/attendance")
    @Operation(
            operationId = "getMonthAttendance",
            summary = "The ascents of a month, route by route (setter+)")
    MonthAttendanceResponse attendance(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Parameter(description = "YYYY-MM, this month by default; never a future month")
                    @RequestParam(required = false)
                    String month) {
        return MonthAttendanceResponse.from(stats.ofMonth(me.memberId(), month));
    }

    @GetMapping("/routes")
    @Operation(
            operationId = "listRouteAttendance",
            summary = "The routes on the wall, with their ascents since they opened (setter+)")
    List<RouteAttendanceResponse> routes(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return stats.onTheWall(me.memberId()).stream().map(RouteAttendanceResponse::from).toList();
    }
}
