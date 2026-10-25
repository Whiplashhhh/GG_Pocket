package fr.gravelinesgrimp.pocket.topo.domain;

import java.util.Set;

/**
 * "Je sais la faire" for one route, in both modes (spec §8.5): known in a mode when ticked by hand
 * or climbed clean in that mode; known in lead means known in top rope.
 */
public record TodoStates(TodoState topRope, TodoState lead) {

    /**
     * @param checked the modes with a "je sais la faire" mark
     * @param cleanAscents the modes with at least one clean ascent
     */
    public static TodoStates of(Set<ClimbMode> checked, Set<ClimbMode> cleanAscents) {
        var lead = stateIn(ClimbMode.LEAD, checked, cleanAscents);
        var topRope =
                lead.known()
                        ? TodoState.FROM_LEAD
                        : stateIn(ClimbMode.TOP_ROPE, checked, cleanAscents);
        return new TodoStates(topRope, lead);
    }

    private static TodoState stateIn(
            ClimbMode mode, Set<ClimbMode> checked, Set<ClimbMode> cleanAscents) {
        if (cleanAscents.contains(mode)) {
            return TodoState.FROM_ASCENT;
        }
        return checked.contains(mode) ? TodoState.CHECKED : TodoState.TODO;
    }
}
