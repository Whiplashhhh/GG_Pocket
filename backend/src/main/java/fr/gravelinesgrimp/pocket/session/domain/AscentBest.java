package fr.gravelinesgrimp.pocket.session.domain;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

/** How well I did a route at best, the best first: clean in lead beats everything. */
public enum AscentBest {
    LEAD_CLEAN,
    LEAD_TOPPED,
    TOP_ROPE_CLEAN,
    TOP_ROPE_TOPPED,
    /** Not topped, but past the half (D-121): still not done. */
    PAST_HALF,
    TRIED;

    public static AscentBest of(ClimbMode mode, boolean topped, boolean clean) {
        return of(mode, topped, clean, false);
    }

    public static AscentBest of(ClimbMode mode, boolean topped, boolean clean, boolean pastHalf) {
        if (!topped) {
            return pastHalf ? PAST_HALF : TRIED;
        }
        if (mode == ClimbMode.LEAD) {
            return clean ? LEAD_CLEAN : LEAD_TOPPED;
        }
        return clean ? TOP_ROPE_CLEAN : TOP_ROPE_TOPPED;
    }

    public boolean topped() {
        return this != TRIED && this != PAST_HALF;
    }

    public boolean inLead() {
        return this == LEAD_CLEAN || this == LEAD_TOPPED;
    }

    public AscentBest bestWith(AscentBest other) {
        return compareTo(other) <= 0 ? this : other;
    }
}
