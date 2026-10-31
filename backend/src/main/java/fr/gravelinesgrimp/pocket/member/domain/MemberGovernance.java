package fr.gravelinesgrimp.pocket.member.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Who may manage whom (spec §5, DECISIONS D-050). Nobody acts on their own account here. The board
 * manages tourists, climbers and setters: it validates new accounts (D-107), switches between
 * tourist and climber, gives or takes the setter role, disables accounts and creates reset links.
 * Only an admin acts on board members and admins, and gives the board or admin role.
 */
public final class MemberGovernance {

    private static final Set<Role> BOARD_ASSIGNABLE =
            EnumSet.of(Role.TOURIST, Role.CLIMBER, Role.SETTER);

    private MemberGovernance() {}

    /** Roles the actor may give to the target (the target's current role excluded). */
    public static Set<Role> assignableRoles(Role actor, boolean self, Role target) {
        if (self) {
            return EnumSet.noneOf(Role.class);
        }
        Set<Role> roles =
                switch (actor) {
                    case ADMIN -> EnumSet.allOf(Role.class);
                    case BOARD ->
                            BOARD_ASSIGNABLE.contains(target)
                                    ? EnumSet.copyOf(BOARD_ASSIGNABLE)
                                    : EnumSet.noneOf(Role.class);
                    case SETTER, CLIMBER, TOURIST -> EnumSet.noneOf(Role.class);
                };
        roles.remove(target);
        return roles;
    }

    /**
     * Delete for good: the admin, or the board refusing an account it has never validated (D-107).
     */
    public static boolean canDelete(Role actor, boolean self, boolean targetValidated) {
        if (self) {
            return false;
        }
        return switch (actor) {
            case ADMIN -> true;
            case BOARD -> !targetValidated;
            case SETTER, CLIMBER, TOURIST -> false;
        };
    }

    /** Disable, enable, create a password reset link. */
    public static boolean canManageAccount(Role actor, boolean self, Role target) {
        if (self) {
            return false;
        }
        return switch (actor) {
            case ADMIN -> true;
            case BOARD -> !target.isAtLeast(Role.BOARD);
            case SETTER, CLIMBER, TOURIST -> false;
        };
    }
}
