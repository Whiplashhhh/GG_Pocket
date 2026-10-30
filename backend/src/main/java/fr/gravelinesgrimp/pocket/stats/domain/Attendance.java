package fr.gravelinesgrimp.pocket.stats.domain;

/**
 * Ascents counted over a period (spec §8.10): every attempt, topped or not. The success rate is the
 * share of topped ascents (an ascent counts as a success once topped, falls or not, as on the
 * podium: D-003, D-104).
 */
public record Attendance(long ascentCount, long toppedCount, long cleanCount, long climberCount) {

    public static final Attendance NONE = new Attendance(0, 0, 0, 0);

    /** Rounded percentage of topped ascents, null without any ascent. */
    public Integer successPercent() {
        return ascentCount == 0 ? null : (int) Math.round(100.0 * toppedCount / ascentCount);
    }
}
