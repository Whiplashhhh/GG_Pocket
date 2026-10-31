package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A badge as shown next to a name: its level is the number of tiers earned, or the times a
 * repeatable badge was earned. Level 0 is not earned (yet, or any more).
 */
public record ShownBadge(Badge badge, long level) {

    public static ShownBadge of(Badge badge, Map<BadgeCode, Long> counts) {
        var codes = badge.codes();
        long level =
                badge.tiered()
                        ? codes.stream().filter(code -> counts.getOrDefault(code, 0L) > 0).count()
                        : counts.getOrDefault(codes.getFirst(), 0L);
        return new ShownBadge(badge, level);
    }

    /** The earned ones among {@code badges}, in their order. */
    public static List<ShownBadge> earned(Collection<Badge> badges, Map<BadgeCode, Long> counts) {
        return badges.stream()
                .map(badge -> of(badge, counts))
                .filter(shown -> shown.level() > 0)
                .toList();
    }
}
