package fr.gravelinesgrimp.pocket.topo.application;

/**
 * The ascents of a route by everyone (spec §8.4). {@code leadPercent} is the rounded share of
 * ascents in lead, null when nobody logged one yet.
 */
public record RouteStatsView(
        int ascentCount, int toppedCount, int climberCount, Integer leadPercent) {

    public static RouteStatsView of(long ascents, long topped, long climbers, long lead) {
        return new RouteStatsView(
                (int) ascents,
                (int) topped,
                (int) climbers,
                ascents == 0 ? null : (int) Math.round(100.0 * lead / ascents));
    }
}
