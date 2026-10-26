package fr.gravelinesgrimp.pocket.topo.domain;

/** Where a route on the wall stands in my "à faire" list, for one mode (spec §8.5). */
public enum TodoState {
    /** Not known yet in this mode: the route is on the list. */
    TODO,
    /** Ticked by hand: can be unticked. */
    CHECKED,
    /** Backed by a clean ascent in this mode: change the session to take it back. */
    FROM_ASCENT,
    /** Top rope only: known in lead, so known in top rope too. */
    FROM_LEAD;

    public boolean known() {
        return this != TODO;
    }
}
