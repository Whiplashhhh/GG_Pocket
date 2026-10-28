package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.domain.ReactionKind;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

@Schema(name = "SessionReactionRequest")
public record SessionReactionRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull ReactionKind kind) {}
