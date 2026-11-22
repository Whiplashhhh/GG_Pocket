package fr.gravelinesgrimp.pocket.session.api;

import fr.gravelinesgrimp.pocket.platform.api.validation.PlainText;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "SessionCommentRequest")
public record SessionCommentRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @PlainText @Size(min = 1, max = 500)
                String body,
        @Schema(description = "The comment of the same session this one answers") UUID replyTo) {}
