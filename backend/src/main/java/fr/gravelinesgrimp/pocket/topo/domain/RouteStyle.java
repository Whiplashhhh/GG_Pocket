package fr.gravelinesgrimp.pocket.topo.domain;

/**
 * Style tags of a route (spec §7, DECISIONS D-076), 5 at most per route. The app shows them in
 * French (Conti, Rési, Physique…). Never rename a constant: stored in the database.
 */
public enum RouteStyle {
    CONTINUITY,
    ENDURANCE,
    PHYSICAL,
    TECHNICAL,
    COMPLEX,
    SLAB,
    OVERHANG,
    CRIMPS,
    JUGS,
    PINCHES,
    SLOPERS,
    DIHEDRAL,
    ROOF,
    REACHY
}
