package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.AuditQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/admin/audit", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audit log")
class AuditController {

    private static final String DEFAULT_PAGE_SIZE = "50";

    private final AuditQueryService service;

    AuditController(AuditQueryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listAuditEntries", summary = "The audit log, newest first (board+)")
    AuditPageResponse list(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        var page = service.page(cursor, size);
        return new AuditPageResponse(
                page.entries().stream().map(AuditEntryResponse::from).toList(), page.nextCursor());
    }
}
