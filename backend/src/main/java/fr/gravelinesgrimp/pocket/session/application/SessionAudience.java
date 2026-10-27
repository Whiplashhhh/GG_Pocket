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

import java.util.UUID;

/**
 * Who reads a climber's sessions (spec §14): the climber, and their friends for the sessions shared
 * with friends, while the climber's account is active. Anyone else gets "not found", exactly like
 * for a session that does not exist: the answer never tells that it exists.
 */
@Component
public class SessionAudience {

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
        return session.visibility() == SessionVisibility.FRIENDS
                && isFriendOf(readerId, session.memberId());
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

    private boolean isFriendOf(UUID readerId, UUID climberId) {
        return friendships.areFriends(readerId, climberId)
                && members.findById(climberId).filter(Member::isActive).isPresent();
    }
}
