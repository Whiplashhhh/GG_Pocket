package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

@Schema(name = "StatusChange")
public record StatusChangeRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull MemberStatus status) {}
