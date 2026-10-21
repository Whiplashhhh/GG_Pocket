package fr.gravelinesgrimp.pocket.topo.domain;

/** Why a route is marked "je sais la faire". Never rename a constant: stored in the database. */
public enum CheckSource {
    /** Ticked by hand on the route sheet. */
    MANUAL,
    /** Deduced from a logged ascent (phase 4). */
    ASCENT
}
