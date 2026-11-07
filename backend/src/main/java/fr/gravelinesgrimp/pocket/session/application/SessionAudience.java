package fr.gravelinesgrimp.pocket.session.application;

import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.domain.ClimbingSession;
import fr.gravelinesgrimp.pocket.session.domain.SessionVisibility;
import fr.gravelinesgrimp.pocket.social.application.Friendships;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Who reads a climber's sessions (spec §14, D-120): the climber; their friends for the sessions
 * shared with friends or with the whole club, while the climber's account is active; and, for the
 * sessions shared with the whole club, the members who take part in the club feed while the climber
 * does too (both validated and active). Anyone else gets "not found", exactly like for a session
 * that does not exist: the answer never tells that it exists.
 *
 * <p>Everything that comes from reading a session goes through here: the session, its photo, its
 * reactions and comments, the climber's sessions on their profile.
 */
@Component
public class SessionAudience {

    /** What a friend reads. */
    static final List<String> SHARED_WITH_FRIENDS =
            List.of(SessionVisibility.FRIENDS.name(), SessionVisibility.CLUB.name());

    /** What a member of the club feed who is not a friend reads. */
    static final List<String> SHARED_WITH_CLUB = List.of(SessionVisibility.CLUB.name());

    private final MemberRepository members;
    private final Friendships friendships;

    public SessionAudience(MemberRepository members, Friendships friendships) {
        this.members = members;
        this.friendships = friendships;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean canRead(UUID readerId, ClimbingSession session) {
        if (session.memberId().equals(readerId)) {
            return true;
        }
        return session.visibility()
                .readableBy(
                        () -> isFriendOf(readerId, session.memberId()),
                        () -> inClubFeedTogether(readerId, session.memberId()));
    }

    /**
     * Before reading the shared sessions of {@code climberId}: the reader must be their friend.
     *
     * @throws BusinessException {@code NOT_FOUND}
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void checkFriendOf(UUID readerId, UUID climberId) {
        if (!isFriendOf(readerId, climberId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "No member");
        }
    }

    /**
     * The visibilities of the sessions of {@code climberId} that someone else reads: those shared
     * with friends or with the club for a friend, those shared with the club for a member of the
     * club feed while the climber takes part too.
     *
     * @throws BusinessException {@code NOT_FOUND} (neither)
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<String> sharedWith(UUID readerId, UUID climberId) {
        if (isFriendOf(readerId, climberId)) {
            return SHARED_WITH_FRIENDS;
        }
        if (inClubFeedTogether(readerId, climberId)) {
            return SHARED_WITH_CLUB;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "No member");
    }

    /**
     * Before reading the club feed (D-120): the reader takes part in it (an account awaiting
     * validation never does, see {@code ValidatedMember}).
     *
     * @throws BusinessException {@code CLUB_FEED_NOT_JOINED}
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void checkInClubFeed(UUID readerId) {
        if (!isInClubFeed(readerId)) {
            throw new BusinessException(ErrorCode.CLUB_FEED_NOT_JOINED, "Not in the club feed");
        }
    }

    private boolean isFriendOf(UUID readerId, UUID climberId) {
        return friendships.areFriends(readerId, climberId)
                && members.findById(climberId).filter(Member::isActive).isPresent();
    }

    private boolean inClubFeedTogether(UUID readerId, UUID climberId) {
        return isInClubFeed(readerId) && isInClubFeed(climberId);
    }

    private boolean isInClubFeed(UUID memberId) {
        return members.findById(memberId).filter(Member::isInClubFeed).isPresent();
    }
}
