package fr.gravelinesgrimp.pocket.topo.domain;

/** Where a route stands (spec §7). Never rename a constant: stored in the database. */
public enum RouteStatus {
    /** On the wall. */
    ACTIVE,
    /** Still on the wall, soon dismounted: members are warned once (spec §8.9). */
    LAST_CHANCE,
    /** Off the wall: kept, read-only, in the archive and in the history of the members. */
    DISMOUNTED
}
