package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.auth.application.DeviceService.DeviceView;
import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.Instant;

@Schema(name = "Device", description = "An open session of the member (\"Mes appareils\").")
public record DeviceResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "Opaque id, not the session id")
                String id,
        @Schema(requiredMode = RequiredMode.REQUIRED) DeviceDescription.Browser browser,
        @Schema(requiredMode = RequiredMode.REQUIRED) DeviceDescription.Platform platform,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant signedInAt,
        @Schema(requiredMode = RequiredMode.REQUIRED) Instant lastActiveAt,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "The device making this request")
                boolean current) {

    static DeviceResponse from(DeviceView view) {
        return new DeviceResponse(
                view.id(),
                view.browser(),
                view.platform(),
                view.signedInAt(),
                view.lastActiveAt(),
                view.current());
    }
}
