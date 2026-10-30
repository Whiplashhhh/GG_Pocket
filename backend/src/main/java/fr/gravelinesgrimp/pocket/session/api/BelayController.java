package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.session.application.BelayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** The sessions where someone named me as their belayer (D-101). */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sessions")
class BelayController {

    private static final String DEFAULT_PAGE_SIZE = "20";

    private final BelayService belays;

    BelayController(BelayService belays) {
        this.belays = belays;
    }

    @GetMapping("/me/belays")
    @Operation(
            operationId = "listMyBelays",
            summary = "The sessions where I held the rope, latest day first (cursor pagination)")
    BelayPageResponse mine(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return BelayPageResponse.from(belays.mine(me.memberId(), cursor, size));
    }

    @DeleteMapping("/sessions/{id}/belayer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "withdrawFromBelay",
            summary = "Take my name back from the routes I belayed in a session")
    void withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        belays.withdraw(me.memberId(), id);
    }
}
