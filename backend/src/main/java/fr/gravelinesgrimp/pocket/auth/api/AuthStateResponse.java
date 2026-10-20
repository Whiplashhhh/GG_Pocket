package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.member.api.CurrentMemberResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "AuthState", description = "Whether this browser is signed in, and as whom.")
public record AuthStateResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean authenticated,
        @Schema(description = "Present when signed in") CurrentMemberResponse member) {}
