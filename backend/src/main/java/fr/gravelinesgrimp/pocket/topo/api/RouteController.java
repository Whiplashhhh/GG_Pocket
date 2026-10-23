package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.topo.application.RoutePhotoService;
import fr.gravelinesgrimp.pocket.topo.application.RouteService;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/** The routes of the topo; writing is for setters and above (checked by the services). */
@RestController
@RequestMapping(path = "/api/v1/routes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class RouteController {

    private final RouteService routes;
    private final RoutePhotoService photos;

    RouteController(RouteService routes, RoutePhotoService photos) {
        this.routes = routes;
        this.photos = photos;
    }

    @GetMapping
    @Operation(operationId = "listRoutes", summary = "Routes on the wall (active and last chance)")
    List<RouteSummaryResponse> list(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return routes.listOnTheWall(me.memberId()).stream()
                .map(RouteSummaryResponse::from)
                .toList();
    }

    @GetMapping("/archive")
    @Operation(
            operationId = "listDismountedRoutes",
            summary = "Dismounted routes, most recently dismounted first")
    List<RouteSummaryResponse> listDismounted(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return routes.listDismounted(me.memberId()).stream()
                .map(RouteSummaryResponse::from)
                .toList();
    }

    @GetMapping("/setters")
    @Operation(operationId = "listSetters", summary = "Active setters, by nickname (setter+)")
    List<MemberRefResponse> listSetters() {
        return routes.listSetters().stream().map(MemberRefResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getRoute", summary = "A route, dismounted ones included")
    RouteDetailResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return RouteDetailResponse.from(routes.get(me.memberId(), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRoute", summary = "A new route (setter+)")
    RouteDetailResponse create(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody RouteSettingsRequest body) {
        return RouteDetailResponse.from(routes.create(me.memberId(), body.toDraft()));
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateRoute", summary = "Edit a route (setter+)")
    RouteDetailResponse update(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody RouteSettingsRequest body) {
        return RouteDetailResponse.from(routes.update(me.memberId(), id, body.toDraft()));
    }

    @PutMapping(path = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "replaceRoutePhoto",
            summary = "Upload the route photo (JPEG, PNG or WebP, 10 MB max, setter+)")
    RouteDetailResponse replacePhoto(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        return RouteDetailResponse.from(photos.replace(me.memberId(), id, file.getBytes()));
    }

    @DeleteMapping("/{id}/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRoutePhoto", summary = "Remove the route photo (setter+)")
    void deletePhoto(@PathVariable UUID id) {
        photos.remove(id);
    }

    @PutMapping("/{id}/last-chance")
    @Operation(operationId = "announceLastChance", summary = "Announce a last chance (setter+)")
    RouteDetailResponse announceLastChance(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody LastChanceSettingsRequest body) {
        return RouteDetailResponse.from(
                routes.announceLastChance(me.memberId(), id, body.plannedDismountOn()));
    }

    @DeleteMapping("/{id}/last-chance")
    @Operation(operationId = "cancelLastChance", summary = "Cancel a last chance (setter+)")
    RouteDetailResponse cancelLastChance(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return RouteDetailResponse.from(routes.cancelLastChance(me.memberId(), id));
    }

    @PostMapping("/{id}/dismount")
    @Operation(operationId = "dismountRoute", summary = "Dismount a route (setter+)")
    RouteDetailResponse dismount(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return RouteDetailResponse.from(routes.dismount(me.memberId(), id));
    }

    @PostMapping("/{id}/remount")
    @Operation(operationId = "remountRoute", summary = "Undo a dismount (setter+)")
    RouteDetailResponse remount(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return RouteDetailResponse.from(routes.remount(me.memberId(), id));
    }
}
