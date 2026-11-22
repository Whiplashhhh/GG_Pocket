package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.notification.application.PushSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** My push notifications (D-128, D-130): my devices and the kinds I want. */
@RestController
@RequestMapping(path = "/api/v1/me/push", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications")
class PushController {

    private final PushSettingsService service;

    PushController(PushSettingsService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            operationId = "getMyPushSettings",
            summary = "Whether push is on, what I want, and whether this device receives it")
    PushSettingsResponse settings(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestParam(required = false) String endpoint) {
        return PushSettingsResponse.from(service.settings(me.memberId(), endpoint));
    }

    @PutMapping("/preferences")
    @Operation(operationId = "saveMyPushPreferences", summary = "Which notifications I want")
    PushPreferencesBody savePreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody PushPreferencesBody body) {
        return PushPreferencesBody.from(service.savePreferences(me.memberId(), body.toDomain()));
    }

    @PutMapping("/devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "addMyPushDevice", summary = "This device receives my notifications")
    void addDevice(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody PushDeviceRequest body) {
        service.addDevice(me.memberId(), body.toDomain());
    }

    @DeleteMapping("/devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "removeMyPushDevice",
            summary = "This device no longer receives my notifications")
    void removeDevice(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestParam String endpoint) {
        service.removeDevice(me.memberId(), endpoint);
    }
}
