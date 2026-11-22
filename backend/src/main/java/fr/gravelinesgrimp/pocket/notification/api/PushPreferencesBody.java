package fr.gravelinesgrimp.pocket.notification.api;

import fr.gravelinesgrimp.pocket.notification.domain.PushPreferences;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/** Which notifications reach my phone (D-128). */
@Schema(name = "PushPreferences")
public record PushPreferencesBody(
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean comments,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean replies,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean reactions,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean friendSessions,
        @Schema(requiredMode = RequiredMode.REQUIRED) boolean friendRequests) {

    static PushPreferencesBody from(PushPreferences preferences) {
        return new PushPreferencesBody(
                preferences.comments(),
                preferences.replies(),
                preferences.reactions(),
                preferences.friendSessions(),
                preferences.friendRequests());
    }

    PushPreferences toDomain() {
        return new PushPreferences(comments, replies, reactions, friendSessions, friendRequests);
    }
}
