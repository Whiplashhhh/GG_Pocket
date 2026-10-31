package fr.gravelinesgrimp.pocket.badge.api;

import fr.gravelinesgrimp.pocket.badge.application.BadgeService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** The badge libraries of the profiles (spec §8.8, §9.5), and my favourite badges (D-106). */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Badges")
class BadgeController {

    private final BadgeService badges;

    BadgeController(BadgeService badges) {
        this.badges = badges;
    }

    @GetMapping("/me/badges")
    @Operation(operationId = "getMyBadges", summary = "My badge library")
    BadgeLibraryResponse mine(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return BadgeLibraryResponse.from(badges.mine(me.memberId()));
    }

    @PutMapping(path = "/me/badges/favorites", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "chooseFavoriteBadges",
            summary = "Choose the badges shown next to my name (up to 3, earned ones)")
    BadgeLibraryResponse chooseFavorites(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody FavoriteBadgesRequest body) {
        return BadgeLibraryResponse.from(badges.chooseFavorites(me.memberId(), body.badges()));
    }

    @GetMapping("/members/{id}/badges")
    @Operation(operationId = "getMemberBadges", summary = "The badge library of a member")
    BadgeLibraryResponse ofMember(@PathVariable UUID id) {
        return BadgeLibraryResponse.from(badges.of(id));
    }
}
