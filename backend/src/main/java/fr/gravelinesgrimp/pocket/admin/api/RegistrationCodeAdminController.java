package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.RegistrationCodeAdminService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@RestController
@RequestMapping(
        path = "/api/v1/admin/registration-codes",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Registration codes")
class RegistrationCodeAdminController {

    private final RegistrationCodeAdminService service;

    RegistrationCodeAdminController(RegistrationCodeAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listRegistrationCodes", summary = "Club codes, newest first (admin)")
    List<RegistrationCodeResponse> list() {
        return service.list().stream().map(RegistrationCodeResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRegistrationCode", summary = "A new club code (admin)")
    RegistrationCodeResponse create(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody RegistrationCodeRequest body) {
        return RegistrationCodeResponse.from(service.create(me.memberId(), body.toSettings()));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "updateRegistrationCode",
            summary = "Rename, limit, deactivate or reactivate a code (admin)")
    RegistrationCodeResponse update(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @PathVariable UUID id,
            @Valid @RequestBody RegistrationCodeRequest body) {
        return RegistrationCodeResponse.from(service.update(me.memberId(), id, body.toSettings()));
    }
}
