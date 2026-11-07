package fr.gravelinesgrimp.pocket.stats.domain;

/**
 * Ascents counted over a period (spec §8.10): every attempt, topped or not. The success rate is the
 * share of clean ascents, topped without a fall (D-117): unlike the podium (D-003), a topped ascent
 * with falls is not a success here.
 */
public record Attendance(long ascentCount, long toppedCount, long cleanCount, long climberCount) {

    public static final Attendance NONE = new Attendance(0, 0, 0, 0);

    /** Rounded percentage of clean ascents, null without any ascent. */
    public Integer successPercent() {
        return ascentCount == 0 ? null : (int) Math.round(100.0 * cleanCount / ascentCount);
    }
}
