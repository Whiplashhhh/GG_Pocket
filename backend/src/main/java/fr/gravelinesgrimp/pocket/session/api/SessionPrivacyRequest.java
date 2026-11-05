package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotNull;

/** Who sees the session, and whether its comment goes: allowed even in a frozen month (D-112). */
@Schema(name = "SessionPrivacyRequest")
public record SessionPrivacyRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull SessionVisibility visibility,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Erase the session's comment")
                boolean removeComment) {}
