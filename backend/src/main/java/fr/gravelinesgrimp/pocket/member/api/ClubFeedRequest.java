package fr.gravelinesgrimp.pocket.member.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(
        name = "ClubFeedRequest",
        description =
                "Take part in the club feed or leave it (D-120): to read it, my sessions shared"
                        + " with the club show in it.")
public record ClubFeedRequest(@Schema(requiredMode = RequiredMode.REQUIRED) boolean inClubFeed) {}
