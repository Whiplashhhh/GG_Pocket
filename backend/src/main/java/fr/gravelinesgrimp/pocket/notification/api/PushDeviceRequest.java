package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.notification.domain.PushSubscription;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** This device's push subscription, as the browser gives it ({@code PushSubscription.toJSON()}). */
@Schema(name = "PushDevice")
public record PushDeviceRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(max = 1000) String endpoint,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Valid Keys keys) {

    @Schema(name = "PushDeviceKeys")
    public record Keys(
            @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(max = 120) String p256dh,
            @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(max = 40) String auth) {}

    PushSubscription toDomain() {
        return new PushSubscription(endpoint.strip(), keys.p256dh().strip(), keys.auth().strip());
    }
}
