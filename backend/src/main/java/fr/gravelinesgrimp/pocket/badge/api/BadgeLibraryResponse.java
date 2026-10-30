package fr.gravelinesgrimp.pocket.badge.api;

import fr.gravelinesgrimp.pocket.badge.application.BadgeLibraryView;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCode;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeFamily;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "BadgeLibrary")
public record BadgeLibraryResponse(
        @Schema(
                        requiredMode = RequiredMode.REQUIRED,
                        description = "Every badge of the catalogue, in its order, earned or not")
                List<Entry> badges) {

    @Schema(name = "BadgeLibraryEntry")
    public record Entry(
            @Schema(requiredMode = RequiredMode.REQUIRED) BadgeCode code,
            @Schema(requiredMode = RequiredMode.REQUIRED) BadgeFamily family,
            @Schema(
                            requiredMode = RequiredMode.REQUIRED,
                            description = "Earned again each time: shown ×N")
                    boolean repeatable,
            @Schema(
                            requiredMode = RequiredMode.REQUIRED,
                            description = "Times earned, 0 when not earned yet")
                    long count,
            LocalDate firstEarnedOn,
            LocalDate lastEarnedOn) {}

    static BadgeLibraryResponse from(BadgeLibraryView view) {
        return new BadgeLibraryResponse(
                view.badges().stream()
                        .map(
                                entry ->
                                        new Entry(
                                                entry.code(),
                                                entry.code().family(),
                                                entry.code().repeatable(),
                                                entry.count(),
                                                entry.firstEarnedOn(),
                                                entry.lastEarnedOn()))
                        .toList());
    }
}
