package fr.gravelinesgrimp.pocket.social.api;

import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(name = "FriendshipStateResult")
public record FriendshipStateResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) FriendshipState friendship) {}
