package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

/** How well I did a route at best, the best first: clean in lead beats everything. */
public enum AscentBest {
    LEAD_CLEAN,
    LEAD_TOPPED,
    TOP_ROPE_CLEAN,
    TOP_ROPE_TOPPED,
    TRIED;

    public static AscentBest of(ClimbMode mode, boolean topped, boolean clean) {
        if (!topped) {
            return TRIED;
        }
        if (mode == ClimbMode.LEAD) {
            return clean ? LEAD_CLEAN : LEAD_TOPPED;
        }
        return clean ? TOP_ROPE_CLEAN : TOP_ROPE_TOPPED;
    }

    public boolean topped() {
        return this != TRIED;
    }

    public boolean inLead() {
        return this == LEAD_CLEAN || this == LEAD_TOPPED;
    }

    public AscentBest bestWith(AscentBest other) {
        return compareTo(other) <= 0 ? this : other;
    }
}
