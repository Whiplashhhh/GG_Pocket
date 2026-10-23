package fr.gravelinesgrimp.pocket.topo.domain;

/** How a route is climbed. Never rename a constant: stored in the database. */
public enum ClimbMode {
    /** "En moulinette": the rope is already at the top. */
    TOP_ROPE,
    /** "En tête": the climber clips the rope on the way up. */
    LEAD
}
