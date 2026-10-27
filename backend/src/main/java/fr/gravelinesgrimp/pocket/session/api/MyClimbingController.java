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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** What sessions add up to: my planning, my logbook, and the logbook of a friend. */
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

    @GetMapping("/members/{id}/logbook")
    @Operation(
            operationId = "getMemberLogbook",
            summary = "A friend's logbook, from the sessions they share with friends")
    LogbookResponse getMemberLogbook(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return LogbookResponse.from(logbook.ofFriend(me.memberId(), id));
    }
}
