package fr.gravelinesgrimp.pocket.social.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.social.domain.Friendship;
import fr.gravelinesgrimp.pocket.social.domain.FriendshipState;
import fr.gravelinesgrimp.pocket.social.infra.FriendshipRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Friend requests and friendships (spec §8.2). Everything goes through my side: I ask someone, I
 * accept or decline what I was asked, I cancel what I asked, I end a friendship. Only active
 * members can be asked; an unknown, disabled or former member is "not found".
 */
@Service
public class FriendshipService {

    private final FriendshipRepository friendships;
    private final MemberRepository members;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public FriendshipService(
            FriendshipRepository friendships,
            MemberRepository members,
            RateLimiter rateLimiter,
            Clock clock) {
        this.friendships = friendships;
        this.members = members;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * Asks {@code otherId} to be friends, or accepts if they already asked me. Asking again what is
     * already asked or accepted changes nothing.
     *
     * @return where we stand afterwards ({@code REQUESTED} or {@code FRIENDS})
     * @throws BusinessException {@code VALIDATION_FAILED} (myself), {@code NOT_FOUND}, {@code
     *     RATE_LIMITED}
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public FriendshipState befriend(UUID me, UUID otherId) {
        if (me.equals(otherId)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Nobody befriends themselves");
        }
        lockPair(me, otherId);
        var existing = friendships.findBetween(me, otherId);
        if (existing.isPresent()) {
            var friendship = existing.get();
            if (!friendship.isAccepted() && friendship.addresseeId().equals(me)) {
                friendship.acceptBy(me, clock.instant());
            }
            return FriendshipState.of(friendship, me);
        }
        rateLimiter.consume(RateLimiter.FRIEND_REQUEST_PER_MEMBER, me.toString());
        return FriendshipState.of(friendships.save(Friendship.request(me, otherId)), me);
    }

    /**
     * Declines their request, cancels mine, or ends our friendship: whatever there is between us
     * goes. Nothing between us is fine too.
     */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional
    public void unfriend(UUID me, UUID otherId) {
        friendships.findBetween(me, otherId).ifPresent(friendships::delete);
    }

    /** Only active members show: a disabled friend comes back when their account does. */
    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public FriendsView overview(UUID me) {
        var mine = friendships.findAllOf(me);
        Map<UUID, Member> others =
                members.findAllById(mine.stream().map(f -> f.otherThan(me)).toList()).stream()
                        .filter(Member::isActive)
                        .collect(Collectors.toMap(Member::id, Function.identity()));
        var incoming = new ArrayList<FriendsView.FriendView>();
        var outgoing = new ArrayList<FriendsView.FriendView>();
        var friends = new ArrayList<FriendsView.FriendView>();
        for (var friendship : mine) {
            var other = others.get(friendship.otherThan(me));
            if (other == null) {
                continue;
            }
            var card = MemberCardView.of(other);
            switch (FriendshipState.of(friendship, me)) {
                case INCOMING ->
                        incoming.add(new FriendsView.FriendView(card, friendship.createdAt()));
                case REQUESTED ->
                        outgoing.add(new FriendsView.FriendView(card, friendship.createdAt()));
                default -> friends.add(new FriendsView.FriendView(card, friendship.acceptedAt()));
            }
        }
        friends.sort((a, b) -> b.since().compareTo(a.since()));
        return new FriendsView(incoming, outgoing, friends);
    }

    /**
     * Locks both members, always in the same order, so that two members asking each other at the
     * same time end up with one friendship, not a failed insert. The other one must be active.
     */
    private void lockPair(UUID me, UUID otherId) {
        var meFirst = me.compareTo(otherId) < 0;
        var first = members.lockById(meFirst ? me : otherId);
        var second = members.lockById(meFirst ? otherId : me);
        var other = meFirst ? second : first;
        if (other.filter(Member::isActive).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "No member");
        }
    }
}
