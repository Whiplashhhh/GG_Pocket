package fr.gravelinesgrimp.pocket.session.domain;

/** Ascents of one route by everyone, from a grouped query. */
public record RouteAscentTotals(
        long ascentCount, long toppedCount, long climberCount, long leadCount) {}
