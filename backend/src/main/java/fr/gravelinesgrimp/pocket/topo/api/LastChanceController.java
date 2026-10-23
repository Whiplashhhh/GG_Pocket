package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.topo.application.LastChanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The last chance pop-up and its follow-up for setters (spec §8.9 and §8.10). */
@RestController
@RequestMapping(path = "/api/v1/routes/last-chance", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class LastChanceController {

    private final LastChanceService service;

    LastChanceController(LastChanceService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            operationId = "listLastChanceRoutes",
            summary = "Routes in last chance, with how many climbers saw it (setter+)")
    List<LastChanceRouteResponse> list(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return service.listWithAcknowledgements(me.memberId()).stream()
                .map(LastChanceRouteResponse::from)
                .toList();
    }

    @GetMapping("/unacknowledged")
    @Operation(
            operationId = "listUnacknowledgedLastChance",
            summary = "Routes in last chance I have not acknowledged yet")
    List<RouteSummaryResponse> listUnacknowledged(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return service.listUnacknowledged(me.memberId()).stream()
                .map(RouteSummaryResponse::from)
                .toList();
    }

    @PostMapping("/acknowledgements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "acknowledgeLastChance", summary = "Acknowledge routes in last chance")
    void acknowledge(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody LastChanceAcknowledgementRequest body) {
        service.acknowledge(me.memberId(), body.routeIds());
    }
}
