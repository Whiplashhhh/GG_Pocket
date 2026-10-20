package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.admin.application.AuditQueryService.AuditEntryView;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "AuditEntry", description = "Who did what, to whom, when.")
public record AuditEntryResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant occurredAt,
        @Schema(description = "Absent if that member was deleted since") UUID actorId,
        @Schema(description = "Nickname at the time of the action") String actorNickname,
        @Schema(requiredMode = RequiredMode.REQUIRED) AuditAction action,
        @Schema(requiredMode = RequiredMode.REQUIRED) AuditTargetType targetType,
        UUID targetId,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Name at the time of the action")
                String targetLabel,
        String before,
        String after) {

    static AuditEntryResponse from(AuditEntryView view) {
        return new AuditEntryResponse(
                view.id(),
                view.occurredAt(),
                view.actorId(),
                view.actorNickname(),
                view.action(),
                view.targetType(),
                view.targetId(),
                view.targetLabel(),
                view.before(),
                view.after());
    }
}
