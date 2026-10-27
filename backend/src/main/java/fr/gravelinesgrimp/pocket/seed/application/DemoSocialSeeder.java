package fr.gravelinesgrimp.pocket.seed.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.social.domain.Friendship;
import fr.gravelinesgrimp.pocket.social.infra.FriendshipRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Demo friendships (spec §17): "grimpeur" has two friends and a request waiting for an answer, so
 * the directory, the dot on the avatar and a friend's profile all have something to show.
 */
@Component
class DemoSocialSeeder {

    private final FriendshipRepository friendships;

    DemoSocialSeeder(FriendshipRepository friendships) {
        this.friendships = friendships;
    }

    /**
     * Seeds the friendships once: does nothing if there are already some.
     *
     * @return the number of friendships and requests created
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int seed(Map<String, Member> accounts, Instant now) {
        if (friendships.count() > 0) {
            return 0;
        }
        befriend(accounts.get("ouvreur"), accounts.get("grimpeur"), now);
        befriend(accounts.get("grimpeur"), accounts.get("direction"), now);
        befriend(accounts.get("direction"), accounts.get("ouvreur"), now);
        friendships.save(
                Friendship.request(accounts.get("admin").id(), accounts.get("grimpeur").id()));
        return 4;
    }

    private void befriend(Member requester, Member addressee, Instant now) {
        var friendship = Friendship.request(requester.id(), addressee.id());
        friendship.acceptBy(addressee.id(), now);
        friendships.save(friendship);
    }
}
