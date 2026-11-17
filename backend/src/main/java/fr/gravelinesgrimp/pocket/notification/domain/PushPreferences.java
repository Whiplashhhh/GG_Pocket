package fr.gravelinesgrimp.pocket.notification.domain;

/** What a member wants on their phone (D-128): one switch per kind, friend requests together. */
public record PushPreferences(
        boolean comments,
        boolean replies,
        boolean reactions,
        boolean friendSessions,
        boolean friendRequests) {

    /** Chosen by Willem (2026-11-17): what is said to me, yes; the rest, on demand. */
    public static final PushPreferences DEFAULTS =
            new PushPreferences(true, true, false, false, true);

    public boolean allows(NotificationKind kind) {
        return switch (kind) {
            case COMMENT -> comments;
            case REPLY -> replies;
            case REACTION -> reactions;
            case FRIEND_SESSION -> friendSessions;
            case FRIEND_REQUEST, FRIEND_ACCEPTED -> friendRequests;
        };
    }
}
