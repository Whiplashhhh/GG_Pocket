package fr.gravelinesgrimp.pocket.account.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.auth.infra.MemberSessionStore;
import fr.gravelinesgrimp.pocket.member.application.MediaService;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.MediaFile;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Nickname;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MediaFileRepository;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.application.RateLimiter;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionCommentRepository;
import fr.gravelinesgrimp.pocket.session.infra.SessionReactionRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Deleting an account for good (spec §14, DECISIONS D-096), by its owner or by the admin. What is
 * personal goes: the member row and, through {@code on delete cascade}, the hearts, the "je sais la
 * faire", the comments and reactions, the friendships, the reset links; the avatar and the session
 * photos are deleted here (files included), and so are the reactions and comments of the others on
 * my sessions. What feeds the routes' statistics stays, handed over to "Ancien·ne membre": the
 * sessions and their ascents (stripped of comment and photo, seen by nobody) and the routes set.
 */
@Service
public class AccountDeletionService {

    private static final EnumSet<MediaUsage> PERSONAL_MEDIA =
            EnumSet.of(MediaUsage.AVATAR, MediaUsage.SESSION_PHOTO);

    private final MemberRepository members;
    private final MediaFileRepository mediaFiles;
    private final MediaService media;
    private final ClimbingSessionRepository sessions;
    private final SessionReactionRepository sessionReactions;
    private final SessionCommentRepository sessionComments;
    private final RouteRepository routes;
    private final MemberSessionStore devices;
    private final AuditLog auditLog;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public AccountDeletionService(
            MemberRepository members,
            MediaFileRepository mediaFiles,
            MediaService media,
            ClimbingSessionRepository sessions,
            SessionReactionRepository sessionReactions,
            SessionCommentRepository sessionComments,
            RouteRepository routes,
            MemberSessionStore devices,
            AuditLog auditLog,
            PasswordEncoder passwordEncoder,
            RateLimiter rateLimiter,
            Clock clock) {
        this.members = members;
        this.mediaFiles = mediaFiles;
        this.media = media;
        this.sessions = sessions;
        this.sessionReactions = sessionReactions;
        this.sessionComments = sessionComments;
        this.routes = routes;
        this.devices = devices;
        this.auditLog = auditLog;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * Self-service, behind the password: a phone left unlocked is not enough. Wrong passwords count
     * like failed sign-ins from this address. The last active admin cannot leave.
     */
    @Transactional
    public void deleteOwnAccount(UUID memberId, String password, String clientAddress) {
        var member = lock(memberId);
        var failureKey = Nickname.key(member.nickname()) + "@" + clientAddress;
        rateLimiter.ensureAllowed(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, failureKey);
        if (!passwordEncoder.matches(password, member.passwordHash())) {
            rateLimiter.record(RateLimiter.LOGIN_FAILURES_PER_NICKNAME_AND_ADDRESS, failureKey);
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_WRONG, "Wrong password");
        }
        erase(member, member);
    }

    /** Spec §5: the admin only, never on their own account (that is the self-service). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteMember(UUID actorId, UUID memberId) {
        if (actorId.equals(memberId)) {
            throw new BusinessException(
                    ErrorCode.SELF_MANAGEMENT_FORBIDDEN, "Members do not manage their own account");
        }
        var actor = lock(actorId);
        erase(actor, lock(memberId));
    }

    private void erase(Member actor, Member member) {
        if (member.role() == Role.ADMIN && member.isActive()) {
            ensureAnotherActiveAdmin(member);
        }
        // Written first: the entry keeps the names once the account is gone (actor_id goes null).
        auditLog.record(
                actor,
                AuditAction.MEMBER_DELETED,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname(),
                null,
                null);
        var now = clock.instant();
        // Before the hand-over, while the sessions are still mine.
        sessionReactions.deleteAllOnSessionsOf(member.id());
        sessionComments.deleteAllOnSessionsOf(member.id());
        sessions.handOver(member.id(), FormerMember.ID, now);
        routes.creditSetter(member.id(), FormerMember.ID);
        member.changeAvatar(null);
        mediaFiles.findAllByOwnerIdAndUsageIn(member.id(), PERSONAL_MEDIA).stream()
                .map(MediaFile::id)
                .forEach(media::delete);
        members.delete(member);
        devices.deleteAllOfAfterCommit(member.id(), null);
    }

    /** Spec §5: there is always at least one active admin. Locks the admins meanwhile. */
    private void ensureAnotherActiveAdmin(Member leaving) {
        var others =
                members.lockAllByRoleAndStatus(Role.ADMIN, MemberStatus.ACTIVE).stream()
                        .filter(admin -> !admin.id().equals(leaving.id()))
                        .count();
        if (others == 0) {
            throw new BusinessException(ErrorCode.LAST_ADMIN, "Last active admin");
        }
    }

    /** "Ancien·ne membre" is out of reach: as if it did not exist. */
    private Member lock(UUID memberId) {
        return members.lockById(memberId)
                .filter(member -> !FormerMember.is(member.id()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
