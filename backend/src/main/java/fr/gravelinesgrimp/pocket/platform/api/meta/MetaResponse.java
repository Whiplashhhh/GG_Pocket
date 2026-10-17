package fr.gravelinesgrimp.pocket.platform.api.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "Meta", description = "What is running on the server.")
public record MetaResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "1.0.0") String version,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        allowableValues = {"local", "dev", "prod"})
                String environment,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Git commit, or 'unknown'")
                String commit,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Git branch, or 'unknown'")
                String branch) {}
