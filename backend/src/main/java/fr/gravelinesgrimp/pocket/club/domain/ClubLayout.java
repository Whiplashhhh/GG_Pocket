package fr.gravelinesgrimp.pocket.club.domain;

import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The whole club configuration at once: a few dozen rows, read in four queries, so that a list of
 * routes never looks up its grade, cord, sector and colours one by one.
 */
public record ClubLayout(
        Map<UUID, Sector> sectors,
        Map<UUID, Cord> cords,
        Map<UUID, HoldColor> holdColors,
        Map<UUID, Grade> grades) {

    public static ClubLayout of(
            List<Sector> sectors,
            List<Cord> cords,
            List<HoldColor> holdColors,
            List<Grade> grades) {
        return new ClubLayout(byId(sectors), byId(cords), byId(holdColors), byId(grades));
    }

    public Optional<Sector> sector(UUID id) {
        return Optional.ofNullable(sectors.get(id));
    }

    public Optional<Cord> cord(UUID id) {
        return Optional.ofNullable(cords.get(id));
    }

    public Optional<HoldColor> holdColor(UUID id) {
        return Optional.ofNullable(holdColors.get(id));
    }

    public Optional<Grade> grade(UUID id) {
        return Optional.ofNullable(grades.get(id));
    }

    public List<Sector> sortedSectors() {
        return sectors.values().stream()
                .sorted(Comparator.comparingInt(Sector::position).thenComparing(Sector::name))
                .toList();
    }

    public List<Cord> sortedCords() {
        return cords.values().stream().sorted(Comparator.comparingInt(Cord::number)).toList();
    }

    public List<HoldColor> sortedHoldColors() {
        return holdColors.values().stream()
                .sorted(
                        Comparator.comparingInt(HoldColor::position)
                                .thenComparing(HoldColor::label))
                .toList();
    }

    public List<Grade> sortedGrades() {
        return grades.values().stream().sorted(Comparator.comparingInt(Grade::rank)).toList();
    }

    private static <T extends TimestampedEntity> Map<UUID, T> byId(List<T> items) {
        return items.stream()
                .collect(Collectors.toUnmodifiableMap(item -> item.id(), Function.identity()));
    }
}
