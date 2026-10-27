package fr.gravelinesgrimp.pocket.social.domain;

import java.util.UUID;

/** Where I stand with another member: what the "Demander en ami" button shows. */
public enum FriendshipState {
    /** Nothing between us. */
    NONE,
    /** I asked, they have not answered yet. */
    REQUESTED,
    /** They asked, I have not answered yet. */
    INCOMING,
    FRIENDS,
    /** That member is me. */
    SELF;

    /** {@code friendship} is the row between {@code me} and someone else, or null. */
    public static FriendshipState of(Friendship friendship, UUID me) {
        if (friendship == null) {
            return NONE;
        }
        if (friendship.isAccepted()) {
            return FRIENDS;
        }
        return friendship.requesterId().equals(me) ? REQUESTED : INCOMING;
    }
}
