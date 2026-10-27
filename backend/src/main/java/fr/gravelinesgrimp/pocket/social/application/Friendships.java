package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;
import fr.gravelinesgrimp.pocket.social.infra.FriendshipRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Who is friends with whom, for the modules that show something to friends only (sessions…). */
@Component
public class Friendships {

    private final FriendshipRepository friendships;

    public Friendships(FriendshipRepository friendships) {
        this.friendships = friendships;
    }

    /** Accepted friendship only: a pending request opens nothing. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean areFriends(UUID a, UUID b) {
        return !a.equals(b) && friendships.areFriends(a, b);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public FriendshipState between(UUID me, UUID other) {
        if (me.equals(other)) {
            return FriendshipState.SELF;
        }
        return FriendshipState.of(friendships.findBetween(me, other).orElse(null), me);
    }
}
