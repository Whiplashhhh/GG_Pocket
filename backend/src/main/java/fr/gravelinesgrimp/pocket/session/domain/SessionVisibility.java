package fr.gravelinesgrimp.pocket.session.domain;

import java.util.function.BooleanSupplier;

/** Who may read a session. Never rename a constant: stored in the database. */
public enum SessionVisibility {
    /** The climber and their friends (the default, spec §7). */
    FRIENDS,
    /** "Juste moi". */
    PRIVATE,
    /**
     * "Tout le club" (D-120): the climber's friends, and the members who take part in the club feed
     * while the climber does too.
     */
    CLUB;

    /**
     * Whether someone other than the climber reads a session with this visibility. Each answer is
     * only asked for when it matters (each may cost a query).
     *
     * @param friend the reader is a friend of the climber, whose account is active
     * @param inClubFeedTogether the reader and the climber both take part in the club feed, with
     *     active, validated accounts
     */
    public boolean readableBy(BooleanSupplier friend, BooleanSupplier inClubFeedTogether) {
        return switch (this) {
            case PRIVATE -> false;
            case FRIENDS -> friend.getAsBoolean();
            case CLUB -> friend.getAsBoolean() || inClubFeedTogether.getAsBoolean();
        };
    }
}
