package fr.gravelinesgrimp.pocket.admin.api;

import fr.gravelinesgrimp.pocket.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

@Schema(name = "RoleChange")
public record RoleChangeRequest(@Schema(requiredMode = RequiredMode.REQUIRED) @NotNull Role role) {}
