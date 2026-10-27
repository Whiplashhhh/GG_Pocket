package fr.gravelinesgrimp.pocket.social.application;

import java.time.Instant;
import java.util.List;

/**
 * My friendships: requests waiting for my answer, the ones I sent, and my friends. Newest first;
 * {@code since} is when the request was sent, or accepted for a friend.
 */
public record FriendsView(
        List<FriendView> incoming, List<FriendView> outgoing, List<FriendView> friends) {

    public record FriendView(MemberCardView member, Instant since) {}
}
