package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A badge as shown next to a name: its level is its highest tier earned (the first is level 1), or
 * the times a repeatable badge was earned. Level 0 is not earned (yet, or any more). The grades are
 * earned one by one (D-111): a climber with a 7a but no 5b on record is level 6.
 */
public record ShownBadge(Badge badge, long level) {

    public static ShownBadge of(Badge badge, Map<BadgeCode, Long> counts) {
        var codes = badge.codes();
        long level = 0;
        if (badge.tiered()) {
            for (var tier = 0; tier < codes.size(); tier++) {
                if (counts.getOrDefault(codes.get(tier), 0L) > 0) {
                    level = tier + 1L;
                }
            }
        } else {
            level = counts.getOrDefault(codes.getFirst(), 0L);
        }
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
