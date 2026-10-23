package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The figures of a session card (spec §8.2): ascents, topped ones, ones in lead, and the hardest
 * topped grade (absent when nothing was topped).
 */
public record SessionTotals(
        int ascentCount, int toppedCount, int leadCount, Optional<Line> hardestTopped) {

    /** One ascent, reduced to what the figures need. */
    public record Line(ClimbMode mode, boolean topped, String grade, int gradeRank) {}

    public static SessionTotals of(List<Line> lines) {
        var topped = lines.stream().filter(Line::topped).toList();
        return new SessionTotals(
                lines.size(),
                topped.size(),
                (int) lines.stream().filter(line -> line.mode() == ClimbMode.LEAD).count(),
                topped.stream().max(Comparator.comparingInt(Line::gradeRank)));
    }
}
