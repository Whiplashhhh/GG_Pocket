package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.account.application.AccountDeletionService;
import fr.gravelinesgrimp.pocket.auth.application.DeviceService;
import fr.gravelinesgrimp.pocket.auth.application.PasswordChangeService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The member's password, open sessions ("Mes appareils") and the account itself. */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me")
class MySecurityController {

    private final PasswordChangeService passwordChange;
    private final DeviceService devices;
    private final AccountDeletionService accountDeletion;
    private final SessionEstablisher sessions;

    MySecurityController(
            PasswordChangeService passwordChange,
            DeviceService devices,
            AccountDeletionService accountDeletion,
            SessionEstablisher sessions) {
        this.passwordChange = passwordChange;
        this.devices = devices;
        this.accountDeletion = accountDeletion;
        this.sessions = sessions;
    }

    @PostMapping("/deletion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteMyAccount",
            summary =
                    "Delete my account for good, behind my password; my sessions stay as"
                            + " \"Ancien·ne membre\" for the routes' statistics")
    void deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody AccountDeletionRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        accountDeletion.deleteOwnAccount(me.memberId(), body.password(), ClientAddress.of(request));
        sessions.signOut(request);
        // Like a sign-out: the phone keeps nothing of the member, cached photos included.
        response.setHeader("Clear-Site-Data", "\"cache\"");
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "changeMyPassword",
            summary = "Change my password; closes my other sessions")
    void changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody PasswordChangeRequest body,
            HttpServletRequest request) {
        passwordChange.change(
                me.memberId(),
                body.currentPassword(),
                body.newPassword(),
                sessionId(request),
                ClientAddress.of(request));
    }

    @GetMapping(path = "/devices", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "listMyDevices", summary = "My open sessions, most recent first")
    List<DeviceResponse> list(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            HttpServletRequest request) {
        return devices.list(me.memberId(), sessionId(request)).stream()
                .map(DeviceResponse::from)
                .toList();
    }

    @DeleteMapping("/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "closeMyDevice", summary = "Close one of my sessions")
    void close(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable String deviceId) {
        devices.close(me.memberId(), deviceId);
    }

    @PostMapping("/devices/close-others")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "closeMyOtherDevices", summary = "Close all my sessions but this one")
    void closeOthers(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            HttpServletRequest request) {
        devices.closeOthers(me.memberId(), sessionId(request));
    }

    private static String sessionId(HttpServletRequest request) {
        var session = request.getSession(false);
        return session == null ? null : session.getId();
    }
}
