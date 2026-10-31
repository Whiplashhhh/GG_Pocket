package fr.gravelinesgrimp.pocket.badge.application;

import fr.gravelinesgrimp.pocket.badge.domain.BadgeCode;
import fr.gravelinesgrimp.pocket.badge.domain.FavoriteBadge;
import fr.gravelinesgrimp.pocket.badge.domain.MemberBadgeCount;
import fr.gravelinesgrimp.pocket.badge.domain.ShownBadge;
import fr.gravelinesgrimp.pocket.badge.infra.BadgeAwardRepository;
import fr.gravelinesgrimp.pocket.badge.infra.FavoriteBadgeRepository;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The favourite badges shown next to names (D-106), at their current level: a favourite lost since
 * it was chosen is left out. Runs in the caller's transaction.
 */
@Component
public class BadgeShowcase {

    private final FavoriteBadgeRepository favorites;
    private final BadgeAwardRepository awards;

    public BadgeShowcase(FavoriteBadgeRepository favorites, BadgeAwardRepository awards) {
        this.favorites = favorites;
        this.awards = awards;
    }

    /** Only the members with a badge to show are in the map. */
    public Map<UUID, List<ShownBadge>> of(Collection<UUID> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        var chosen =
                favorites.findAllByMemberIdIn(memberIds).stream()
                        .collect(
                                Collectors.groupingBy(
                                        FavoriteBadge::memberId,
                                        Collectors.mapping(
                                                FavoriteBadge::badge, Collectors.toList())));
        if (chosen.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Map<BadgeCode, Long>> counts = new HashMap<>();
        for (MemberBadgeCount count : awards.countPerMemberAndCode(chosen.keySet())) {
            counts.computeIfAbsent(count.memberId(), id -> new EnumMap<>(BadgeCode.class))
                    .put(count.code(), count.count());
        }
        Map<UUID, List<ShownBadge>> shown = new HashMap<>();
        chosen.forEach(
                (memberId, badges) -> {
                    var earned = ShownBadge.earned(badges, counts.getOrDefault(memberId, Map.of()));
                    if (!earned.isEmpty()) {
                        shown.put(memberId, earned);
                    }
                });
        return shown;
    }
}
