package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.notification.application.PushSettingsService.PushSettingsView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "PushSettings")
public record PushSettingsResponse(
        @Schema(description = "The server's VAPID key; absent when push is off") String publicKey,
        @Schema(requiredMode = RequiredMode.REQUIRED) PushPreferencesBody preferences,
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Whether the device asked about receives my notifications")
                boolean thisDevice) {

    static PushSettingsResponse from(PushSettingsView view) {
        return new PushSettingsResponse(
                view.publicKey(), PushPreferencesBody.from(view.preferences()), view.thisDevice());
    }
}
