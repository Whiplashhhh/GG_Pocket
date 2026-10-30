package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * What changes between the awards stored and the ones the history gives now (spec §9.5): new ones
 * (the badges just unlocked), the ones whose condition no longer holds, and the ones that moved to
 * another session (its session was changed or deleted, a later one met the condition again).
 */
public record AwardDiff(List<Award> added, List<Award.Id> removed, List<Award> moved) {

    public static AwardDiff between(Collection<Award> stored, Collection<Award> computed) {
        Map<Award.Id, Award> before =
                stored.stream().collect(Collectors.toMap(Award::id, Function.identity()));
        Map<Award.Id, Award> after =
                computed.stream().collect(Collectors.toMap(Award::id, Function.identity()));
        var added = computed.stream().filter(award -> !before.containsKey(award.id())).toList();
        var removed = stored.stream().map(Award::id).filter(id -> !after.containsKey(id)).toList();
        var moved =
                computed.stream()
                        .filter(
                                award -> {
                                    var old = before.get(award.id());
                                    return old != null && !old.equals(award);
                                })
                        .toList();
        return new AwardDiff(added, removed, moved);
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty() && moved.isEmpty();
    }
}
