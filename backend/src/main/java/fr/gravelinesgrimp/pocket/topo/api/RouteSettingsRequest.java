package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.topo.domain.RouteDraft;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStyle;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "RouteSettings", description = "At least one setter or a setter note.")
public record RouteSettingsRequest(
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotBlank @Size(min = 1, max = 60)
                String name,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull UUID gradeId,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull UUID cordId,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(min = 1, max = 2)
                List<@NotNull UUID> holdColorIds,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull LocalDate openedOn,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull List<@NotNull UUID> setterIds,
        @Size(max = 60) String setterNote,
        @Size(max = 500) String advice,
        @Schema(requiredMode = RequiredMode.REQUIRED) @NotNull @Size(max = 5)
                List<@NotNull RouteStyle> styles) {

    RouteDraft toDraft() {
        return new RouteDraft(
                name,
                gradeId,
                cordId,
                holdColorIds,
                openedOn,
                setterIds.stream().distinct().toList(),
                setterNote,
                advice,
                styles);
    }
}
