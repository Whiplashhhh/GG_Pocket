package fr.gravelinesgrimp.pocket.ranking.domain;

/** The two monthly rankings (spec §9.2, D-003, D-004). */
public enum RankingKind {
    /** Climbing days in the month: two sessions on the same day count once. */
    SESSIONS,
    /** Topped ascents in the month, every mode, repeats included. */
    ASCENTS
}
