package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.social.application.FriendshipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** My friend requests and friendships. Everything is on my side: no friendship id to guess. */
@RestController
@RequestMapping(path = "/api/v1/me/friends", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Social")
class FriendshipController {

    private final FriendshipService service;

    FriendshipController(FriendshipService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "getMyFriends", summary = "My friends and pending requests")
    FriendsResponse overview(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return FriendsResponse.from(service.overview(me.memberId()));
    }

    @PutMapping("/{memberId}")
    @Operation(
            operationId = "befriend",
            summary = "Ask a member to be friends, or accept their request")
    FriendshipStateResponse befriend(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID memberId) {
        return new FriendshipStateResponse(service.befriend(me.memberId(), memberId));
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "unfriend",
            summary = "Decline their request, cancel mine, or end our friendship")
    void unfriend(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID memberId) {
        service.unfriend(me.memberId(), memberId);
    }
}
