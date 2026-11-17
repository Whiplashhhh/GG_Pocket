package fr.gravelinesgrimp.pocket.badge.domain;

/** How a badge is earned (spec §9.5): the four tiered families, then the repeatable ones. */
public enum BadgeFamily {
    /** Cumulated topped ascents, every mode, repeats included. */
    TOPPED,
    /** Cumulated topped ascents in lead. */
    LEAD,
    /**
     * First clean ascent (« enchaînée », any mode) of a route of the tier's grade (D-111, D-128).
     */
    FIRST_GRADE,
    /** Cumulated climbing days. */
    CLIMBING_DAYS,
    /** Earned again each time its condition comes back, shown "×N". */
    REPEATABLE
}
