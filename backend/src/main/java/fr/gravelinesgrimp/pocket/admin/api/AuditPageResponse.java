package fr.gravelinesgrimp.pocket.admin.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(name = "AuditPage", description = "Newest first.")
public record AuditPageResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<AuditEntryResponse> entries,
        @Schema(description = "Pass it as ?cursor= for the next page; absent on the last page")
                String nextCursor) {}
