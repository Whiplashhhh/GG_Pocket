package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.account.application.AccountDeletionService;
import fr.gravelinesgrimp.pocket.admin.application.MemberAdminService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Members table and sheet, for the board and the admin. */
@RestController
@RequestMapping(path = "/api/v1/admin/members", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Member management")
class MemberAdminController {

    private final MemberAdminService service;
    private final AccountDeletionService accountDeletion;

    MemberAdminController(MemberAdminService service, AccountDeletionService accountDeletion) {
        this.service = service;
        this.accountDeletion = accountDeletion;
    }

    @GetMapping
    @Operation(operationId = "listMembers", summary = "Every member, sorted by name (board+)")
    List<MemberSummaryResponse> list() {
        return service.list().stream().map(MemberSummaryResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getMember", summary = "A member sheet and allowed actions (board+)")
    MemberDetailResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        return MemberDetailResponse.from(service.get(me.memberId(), id));
    }

    @PutMapping("/{id}/role")
    @Operation(operationId = "changeMemberRole", summary = "Give a role (role matrix, spec §5)")
    MemberDetailResponse changeRole(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody RoleChangeRequest body) {
        return MemberDetailResponse.from(service.changeRole(me.memberId(), id, body.role()));
    }

    @PutMapping("/{id}/status")
    @Operation(
            operationId = "changeMemberStatus",
            summary = "Disable (closes all sessions) or enable an account")
    MemberDetailResponse changeStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody StatusChangeRequest body) {
        return MemberDetailResponse.from(service.changeStatus(me.memberId(), id, body.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteMember",
            summary =
                    "Delete an account for good (admin); its sessions stay as \"Ancien·ne"
                            + " membre\"")
    void delete(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        accountDeletion.deleteMember(me.memberId(), id);
    }

    @PostMapping("/{id}/password-reset-link")
    @Operation(
            operationId = "createPasswordResetLink",
            summary = "A single-use reset link valid 24 hours; replaces the previous one")
    PasswordResetLinkResponse createResetLink(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id) {
        var link = service.createResetLink(me.memberId(), id);
        return new PasswordResetLinkResponse(link.token(), link.expiresAt());
    }
}
