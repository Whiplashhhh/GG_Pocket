package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.topo.application.FavoriteService;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** My marks on routes: hearts, "je sais la faire", and the favourite route of my profile. */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class FavoriteController {

    private final FavoriteService service;

    FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @PutMapping("/routes/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "addFavorite", summary = "Heart a route (2 active favourites at most)")
    void addFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody FavoriteRequest body) {
        service.add(me.memberId(), id, body.replaceRouteId());
    }

    @DeleteMapping("/routes/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removeFavorite", summary = "Take my heart back")
    void removeFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        service.remove(me.memberId(), id);
    }

    @GetMapping("/me/favorites")
    @Operation(
            operationId = "listMyFavorites",
            summary = "My favourites, dismounted ones included, newest first")
    List<RouteSummaryResponse> listMine(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return service.listMine(me.memberId()).stream().map(RouteSummaryResponse::from).toList();
    }

    @PutMapping("/routes/{id}/checks/{mode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "checkRoute", summary = "I can climb it in this mode")
    void check(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @PathVariable ClimbMode mode) {
        service.check(me.memberId(), id, mode);
    }

    @DeleteMapping("/routes/{id}/checks/{mode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "uncheckRoute", summary = "Take that back")
    void uncheck(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @PathVariable ClimbMode mode) {
        service.uncheck(me.memberId(), id, mode);
    }

    @PutMapping("/me/favorite-route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "setFavoriteRoute", summary = "Choose my favourite route")
    void setFavoriteRoute(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody FavoriteRouteRequest body) {
        service.chooseFavoriteRoute(me.memberId(), body.routeId());
    }

    @DeleteMapping("/me/favorite-route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "clearFavoriteRoute", summary = "Clear my favourite route")
    void clearFavoriteRoute(@Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        service.clearFavoriteRoute(me.memberId());
    }
}
