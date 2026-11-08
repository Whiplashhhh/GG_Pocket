package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClosedDayService.ClosedDayView;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;

@Schema(name = "ClosedDay")
public record ClosedDayResponse(
        @Schema(requiredMode = RequiredMode.REQUIRED) LocalDate date,
        @Schema(example = "Vacances de Noël") String label) {

    static ClosedDayResponse from(ClosedDayView view) {
        return new ClosedDayResponse(view.date(), view.label());
    }
}
