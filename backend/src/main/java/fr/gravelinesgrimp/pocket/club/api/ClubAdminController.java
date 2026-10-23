package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Sectors, cords and hold colours (admin, spec §8.11). Grades are a fixed list (D-074). */
@RestController
@RequestMapping(path = "/api/v1/admin/club", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Club admin")
class ClubAdminController {

    private final ClubConfigService service;

    ClubAdminController(ClubConfigService service) {
        this.service = service;
    }

    @PostMapping("/sectors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createSector", summary = "A new sector (admin)")
    SectorResponse createSector(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody SectorSettingsRequest body) {
        return SectorResponse.from(service.createSector(me.memberId(), body.toSettings()));
    }

    @PutMapping("/sectors/{id}")
    @Operation(operationId = "updateSector", summary = "Rename or describe a sector (admin)")
    SectorResponse updateSector(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody SectorSettingsRequest body) {
        return SectorResponse.from(service.updateSector(me.memberId(), id, body.toSettings()));
    }

    @PutMapping("/sector-order")
    @Operation(operationId = "reorderSectors", summary = "Reorder the sectors (admin)")
    TopoConfigResponse reorderSectors(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody SectorOrderRequest body) {
        return TopoConfigResponse.from(service.reorderSectors(me.memberId(), body.sectorIds()));
    }

    @PostMapping("/cords")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createCord", summary = "A new cord (admin)")
    CordResponse createCord(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody CordSettingsRequest body) {
        return CordResponse.from(service.createCord(me.memberId(), body.toSettings()));
    }

    @PutMapping("/cords/{id}")
    @Operation(
            operationId = "updateCord",
            summary = "Renumber, move, deactivate or reactivate a cord (admin)")
    CordResponse updateCord(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody CordSettingsRequest body) {
        return CordResponse.from(service.updateCord(me.memberId(), id, body.toSettings()));
    }

    @PostMapping("/hold-colors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createHoldColor", summary = "A new hold colour (admin)")
    HoldColorResponse createHoldColor(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody HoldColorSettingsRequest body) {
        return HoldColorResponse.from(service.createHoldColor(me.memberId(), body.toSettings()));
    }

    @PutMapping("/hold-colors/{id}")
    @Operation(
            operationId = "updateHoldColor",
            summary = "Change, deactivate or reactivate a hold colour (admin)")
    HoldColorResponse updateHoldColor(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody HoldColorSettingsRequest body) {
        return HoldColorResponse.from(
                service.updateHoldColor(me.memberId(), id, body.toSettings()));
    }
}
