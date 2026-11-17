package fr.gravelinesgrimp.pocket.notification.domain;

/** What happened, for the bell (D-128). */
public enum NotificationKind {
    /** Someone reacted to my session. */
    REACTION,
    /** Someone commented on my session. */
    COMMENT,
    /** Someone answered my comment, under my session or someone else's. */
    REPLY,
    /** A friend shared a session. */
    FRIEND_SESSION,
    /** Someone asks to be my friend. */
    FRIEND_REQUEST,
    /** Someone accepted my friend request. */
    FRIEND_ACCEPTED
}
