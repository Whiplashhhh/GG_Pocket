package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClosedDayService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** The days the gym is closed (board and admin, D-116). */
@RestController
@RequestMapping(path = "/api/v1/admin/closed-days", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Club admin")
class ClosedDayController {

    private final ClosedDayService service;

    ClosedDayController(ClosedDayService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            operationId = "listClosedDays",
            summary = "Every closed day, earliest first (board+)")
    List<ClosedDayResponse> list() {
        return service.list().stream().map(ClosedDayResponse::from).toList();
    }

    @PostMapping
    @Operation(
            operationId = "closeDays",
            summary = "Close a day or a range of days, 366 at most (board+); every closed day back")
    List<ClosedDayResponse> close(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody ClosedDaysRequest body) {
        return service.close(me.memberId(), body.from(), body.to(), body.label()).stream()
                .map(ClosedDayResponse::from)
                .toList();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "reopenDays", summary = "Reopen a day or a range of days (board+)")
    void reopen(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Parameter(description = "First day to reopen")
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @Parameter(description = "Last day to reopen, included; the single day from by default")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        service.reopen(me.memberId(), from, to);
    }
}
