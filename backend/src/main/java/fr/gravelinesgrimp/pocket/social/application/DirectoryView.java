package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;

import java.util.List;

/**
 * The club directory (spec §8.2): {@code climberCount} active members, me included, and every other
 * one with where I stand with them.
 */
public record DirectoryView(long climberCount, List<EntryView> members) {

    public record EntryView(MemberCardView member, FriendshipState friendship) {}
}
