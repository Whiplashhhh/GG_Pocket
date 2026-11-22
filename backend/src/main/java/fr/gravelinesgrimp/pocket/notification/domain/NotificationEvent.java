package fr.gravelinesgrimp.pocket.notification.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Something happened that {@code recipientId} may want on their phone (D-128): published by the
 * services once it is saved, sent by push after the commit. The bell does not need it (it reads the
 * tables). {@code reaction} is a reaction kind name; the session fields are null for a friendship.
 */
public record NotificationEvent(
        UUID recipientId,
        NotificationKind kind,
        UUID actorId,
        UUID sessionId,
        LocalDate climbedOn,
        String reaction,
        String excerpt) {

    /** Long enough to recognise a comment on a lock screen. */
    public static final int EXCERPT_LENGTH = 140;

    public static NotificationEvent aboutSession(
            UUID recipientId,
            NotificationKind kind,
            UUID actorId,
            UUID sessionId,
            LocalDate climbedOn,
            String reaction,
            String text) {
        return new NotificationEvent(
                recipientId, kind, actorId, sessionId, climbedOn, reaction, excerpt(text));
    }

    public static NotificationEvent aboutFriendship(
            UUID recipientId, NotificationKind kind, UUID actorId) {
        return new NotificationEvent(recipientId, kind, actorId, null, null, null, null);
    }

    private static String excerpt(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= EXCERPT_LENGTH ? text : text.substring(0, EXCERPT_LENGTH) + "…";
    }
}
