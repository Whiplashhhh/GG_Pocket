package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.domain.BadgeCode;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCount;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Every badge of the catalogue, in its order, with what the climber earned (spec §8.8): {@code
 * count} is 0 and the dates null for a badge not earned yet.
 */
public record BadgeLibraryView(List<Entry> badges) {

    public record Entry(
            BadgeCode code, long count, LocalDate firstEarnedOn, LocalDate lastEarnedOn) {}

    static BadgeLibraryView of(Collection<BadgeCount> earned) {
        Map<BadgeCode, BadgeCount> byCode =
                earned.stream().collect(Collectors.toMap(BadgeCount::code, Function.identity()));
        return new BadgeLibraryView(
                Arrays.stream(BadgeCode.values())
                        .map(
                                code -> {
                                    var found = byCode.get(code);
                                    return found == null
                                            ? new Entry(code, 0, null, null)
                                            : new Entry(
                                                    code,
                                                    found.count(),
                                                    found.firstEarnedOn(),
                                                    found.lastEarnedOn());
                                })
                        .toList());
    }
}
