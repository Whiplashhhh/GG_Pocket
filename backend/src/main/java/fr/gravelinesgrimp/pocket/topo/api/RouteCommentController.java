package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.topo.application.RouteCommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class RouteCommentController {

    private final RouteCommentService service;

    RouteCommentController(RouteCommentService service) {
        this.service = service;
    }

    @GetMapping("/routes/{id}/comments")
    @Operation(operationId = "listRouteComments", summary = "Comments on a route, oldest first")
    List<RouteCommentResponse> list(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return service.list(me.memberId(), id).stream().map(RouteCommentResponse::from).toList();
    }

    @PostMapping("/routes/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addRouteComment", summary = "Comment on a route")
    RouteCommentResponse add(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody RouteCommentRequest body) {
        return RouteCommentResponse.from(service.add(me.memberId(), id, body.body()));
    }

    @DeleteMapping("/route-comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteRouteComment",
            summary = "Delete my comment, or anyone's (board+)")
    void delete(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        service.delete(me.memberId(), id);
    }
}
