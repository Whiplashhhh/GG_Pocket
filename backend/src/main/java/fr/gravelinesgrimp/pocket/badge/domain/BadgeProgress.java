package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.util.List;

/**
 * Where a climber stands on the counted tiers (spec §9.5), for the progress bar to the next level:
 * counted like {@link BadgeEngine} does, on the whole history.
 */
public record BadgeProgress(long topped, long lead, long climbingDays) {

    public static BadgeProgress of(List<ClimbRecord> history) {
        var lines = history.stream().flatMap(session -> session.ascents().stream()).toList();
        return new BadgeProgress(
                lines.stream().filter(ClimbRecord.Line::topped).count(),
                lines.stream()
                        .filter(line -> line.topped() && line.mode() == ClimbMode.LEAD)
                        .count(),
                history.stream().map(ClimbRecord::climbedOn).distinct().count());
    }
}
