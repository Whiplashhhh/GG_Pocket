package fr.gravelinesgrimp.pocket;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.social.domain.Friendship;
import fr.gravelinesgrimp.pocket.social.infra.FriendshipRepository;

import org.springframework.boot.test.context.TestComponent;

import java.time.Clock;

/** Friendships made straight in the database, for tests about what friends may see. */
@TestComponent
public class FriendshipFixtures {

    private final FriendshipRepository friendships;
    private final Clock clock;

    public FriendshipFixtures(FriendshipRepository friendships, Clock clock) {
        this.friendships = friendships;
        this.clock = clock;
    }

    public Friendship befriend(Member requester, Member addressee) {
        var friendship = Friendship.request(requester.id(), addressee.id());
        friendship.acceptBy(addressee.id(), clock.instant());
        return friendships.saveAndFlush(friendship);
    }

    /** Unfriending, declining or cancelling: the row goes. */
    public void end(Friendship friendship) {
        friendships.delete(friendship);
        friendships.flush();
    }

    public Friendship ask(Member requester, Member addressee) {
        return friendships.saveAndFlush(Friendship.request(requester.id(), addressee.id()));
    }
}
