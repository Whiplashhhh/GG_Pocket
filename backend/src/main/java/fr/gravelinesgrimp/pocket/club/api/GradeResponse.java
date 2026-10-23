package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.GradeView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

@Schema(name = "Grade")
public record GradeResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = RequiredMode.REQUIRED, example = "6a+") String code,
        @Schema(requiredMode = RequiredMode.REQUIRED, description = "1 for the easiest grade")
                int rank) {

    static GradeResponse from(GradeView view) {
        return new GradeResponse(view.id(), view.code(), view.rank());
    }
}
