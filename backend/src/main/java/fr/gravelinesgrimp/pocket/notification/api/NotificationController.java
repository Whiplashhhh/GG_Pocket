package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.notification.application.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** My bell (D-128). Everything is on my side: no id to guess. */
@RestController
@RequestMapping(path = "/api/v1/me/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications")
class NotificationController {

    private final NotificationService service;

    NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            operationId = "getMyNotifications",
            summary =
                    "What happened to me lately (reactions, comments, answers, friends' sessions,"
                            + " friend requests), newest first, and how much is new")
    NotificationsResponse mine(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return NotificationsResponse.from(service.mine(me.memberId()));
    }

    @PutMapping("/seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "markMyNotificationsSeen", summary = "I opened the bell")
    void seen(@Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        service.markSeen(me.memberId());
    }
}
