package fr.gravelinesgrimp.pocket.topo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RouteCommentRequest")
public record RouteCommentRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(min = 1, max = 500)
                String body) {}
