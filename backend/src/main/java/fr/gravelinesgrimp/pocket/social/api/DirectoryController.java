package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.social.application.DirectoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** The club directory and the public profiles: what every member may see of the others. */
@RestController
@RequestMapping(path = "/api/v1/members", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Social")
class DirectoryController {

    private final DirectoryService service;

    DirectoryController(DirectoryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "getDirectory", summary = "Every active member, by name")
    DirectoryResponse directory(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return DirectoryResponse.from(service.directory(me.memberId()));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getMemberProfile", summary = "The public profile of a member")
    MemberProfileResponse profile(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return MemberProfileResponse.from(service.profile(me.memberId(), id));
    }
}
