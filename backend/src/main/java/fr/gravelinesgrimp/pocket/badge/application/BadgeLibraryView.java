package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.domain.Badge;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCode;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeCount;
import fr.gravelinesgrimp.pocket.badge.domain.BadgeProgress;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Every badge of the catalogue, in its order, with what the climber earned (spec §8.8): {@code
 * count} is 0 and the dates null for a badge not earned yet; another member's dates are the first
 * of their month. {@code favorites}: the badges they show next to their name, earned ones only
 * (D-106). {@code progress}: only in my own library.
 */
public record BadgeLibraryView(List<Entry> badges, List<Badge> favorites, BadgeProgress progress) {

    public record Entry(
            BadgeCode code, long count, LocalDate firstEarnedOn, LocalDate lastEarnedOn) {}

    static BadgeLibraryView of(
            Collection<BadgeCount> earned, List<Badge> favorites, BadgeProgress progress) {
        Map<BadgeCode, BadgeCount> byCode =
                earned.stream().collect(Collectors.toMap(BadgeCount::code, Function.identity()));
        var entries =
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
                        .toList();
        Map<BadgeCode, Long> counts =
                earned.stream().collect(Collectors.toMap(BadgeCount::code, BadgeCount::count));
        var shown =
                favorites.stream()
                        .filter(badge -> badge.codes().stream().anyMatch(counts::containsKey))
                        .toList();
        return new BadgeLibraryView(entries, shown, progress);
    }

    /**
     * Another member's library: dates to the month only. Badges count "juste moi" sessions too
     * (D-105), and a day would date a private session for every member (security review, D-112).
     */
    BadgeLibraryView toMonths() {
        return new BadgeLibraryView(
                badges.stream()
                        .map(
                                entry ->
                                        new Entry(
                                                entry.code(),
                                                entry.count(),
                                                monthOf(entry.firstEarnedOn()),
                                                monthOf(entry.lastEarnedOn())))
                        .toList(),
                favorites,
                progress);
    }

    private static LocalDate monthOf(LocalDate day) {
        return day == null ? null : day.withDayOfMonth(1);
    }
}
