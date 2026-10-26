package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.session.application.LogbookService;
import fr.gravelinesgrimp.pocket.session.application.PlanningService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * What my sessions add up to: the planning and the logbook. Mine only for now (phase 5: friends).
 */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sessions")
class MyClimbingController {

    private final PlanningService planning;
    private final LogbookService logbook;

    MyClimbingController(PlanningService planning, LogbookService logbook) {
        this.planning = planning;
        this.logbook = logbook;
    }

    @GetMapping("/me/planning")
    @Operation(
            operationId = "getMyPlanning",
            summary = "One month of my planning: days climbed, month summary, current streak")
    PlanningResponse getPlanning(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Parameter(description = "YYYY-MM, this month by default; never a future month")
                    @RequestParam(required = false)
                    String month) {
        return PlanningResponse.from(planning.of(me.memberId(), month));
    }

    @GetMapping("/me/logbook")
    @Operation(
            operationId = "getMyLogbook",
            summary = "My logbook: totals, grade pyramid and every route I climbed")
    LogbookResponse getLogbook(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return LogbookResponse.from(logbook.of(me.memberId()));
    }
}
