package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService.TopoConfigView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(
        name = "TopoConfig",
        description = "The club layout: sectors and their cords, hold colours, grades.")
public record TopoConfigResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) List<SectorResponse> sectors,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<HoldColorResponse> holdColors,
        @Schema(requiredMode = RequiredMode.REQUIRED) List<GradeResponse> grades) {

    static TopoConfigResponse from(TopoConfigView view) {
        return new TopoConfigResponse(
                view.sectors().stream().map(SectorResponse::from).toList(),
                view.holdColors().stream().map(HoldColorResponse::from).toList(),
                view.grades().stream().map(GradeResponse::from).toList());
    }
}
