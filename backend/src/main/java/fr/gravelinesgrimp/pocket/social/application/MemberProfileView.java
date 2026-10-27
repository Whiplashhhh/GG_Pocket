package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;
import fr.gravelinesgrimp.pocket.topo.application.RouteSummaryView;

import java.util.List;
import java.util.UUID;

/**
 * Another member's profile as any member sees it (spec §8.8): the public part only. {@code age} is
 * there only if they chose to show it; their sessions and logbook are read elsewhere, by friends.
 */
public record MemberProfileView(
        MemberCardView member,
        Integer age,
        Integer climbingSinceYear,
        String bio,
        UUID favoriteRouteId,
        List<RouteSummaryView> favorites,
        FriendshipState friendship) {}
