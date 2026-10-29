package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.topo.application.MemberRefView;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A session where I held the rope, as I see it in "Tes assurages": who climbed, when, on how many
 * routes, and whether I may open the session (naming me gives me no right on it, D-101).
 */
public record BelayView(
        UUID sessionId,
        MemberRefView climber,
        LocalDate climbedOn,
        int routeCount,
        boolean readable) {

    /** {@code nextCursor} is null on the last page. */
    public record Page(List<BelayView> items, String nextCursor) {}
}
