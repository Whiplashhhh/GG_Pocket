package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.auth.application.DeviceService;
import fr.gravelinesgrimp.pocket.auth.application.PasswordResetService;
import fr.gravelinesgrimp.pocket.auth.application.PasswordResetService.ResetLink;
import fr.gravelinesgrimp.pocket.member.domain.Age;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.MemberGovernance;
import fr.gravelinesgrimp.pocket.member.domain.MemberStatus;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository;
import fr.gravelinesgrimp.pocket.session.infra.ClimbingSessionRepository.SessionCount;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Member management by the board and the admin (spec §5 and §8.11). Every method is reserved to the
 * board and above; what may be done to a given member follows {@link MemberGovernance}.
 */
@Service
public class MemberAdminService {

    public record MemberSummaryView(
            UUID id,
            String nickname,
            String firstName,
            String lastName,
            Role role,
            MemberStatus status,
            boolean validated,
            Boolean declaredLicensed,
            UUID avatarId,
            Instant createdAt,
            Instant lastSeenAt,
            long sessionCount) {}

    /** What the actor may do to this member, so the screen only offers allowed actions. */
    public record Permissions(
            Set<Role> assignableRoles,
            boolean canDisable,
            boolean canEnable,
            boolean canCreateResetLink,
            boolean canDelete,
            boolean canValidate) {}

    /**
     * The member sheet (DECISIONS D-045): no birth date, only whether a parental consent covers a
     * member under 15. The join note is there until the board decides on the account (D-107).
     */
    public record MemberDetailView(
            MemberSummaryView summary,
            String email,
            String joinNote,
            boolean underFifteenWithParentalConsent,
            Integer clubArrivalYear,
            Integer climbingSinceYear,
            Permissions permissions) {}

    private final MemberRepository members;
    private final ClimbingSessionRepository sessions;
    private final DeviceService devices;
    private final PasswordResetService passwordResets;
    private final AuditLog auditLog;
    private final Clock clock;

    public MemberAdminService(
            MemberRepository members,
            ClimbingSessionRepository sessions,
            DeviceService devices,
            PasswordResetService passwordResets,
            AuditLog auditLog,
            Clock clock) {
        this.members = members;
        this.sessions = sessions;
        this.devices = devices;
        this.passwordResets = passwordResets;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional(readOnly = true)
    public List<MemberSummaryView> list() {
        Map<UUID, Long> sessionCounts =
                sessions.countByMember().stream()
                        .collect(
                                Collectors.toMap(
                                        SessionCount::getMemberId, SessionCount::getSessions));
        return members.findAllForDirectory().stream()
                .map(member -> summary(member, sessionCounts.getOrDefault(member.id(), 0L)))
                .toList();
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional(readOnly = true)
    public MemberDetailView get(UUID actorId, UUID memberId) {
        var actor = load(actorId);
        return detail(actor, load(memberId));
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public MemberDetailView changeRole(UUID actorId, UUID memberId, Role role) {
        var actor = load(actorId);
        var member = load(memberId);
        if (member.role() == role) {
            return detail(actor, member);
        }
        checkNotSelf(actor, member);
        if (!member.isValidated()) {
            throw new BusinessException(
                    ErrorCode.MEMBER_AWAITING_VALIDATION, "Validate the account before");
        }
        if (!MemberGovernance.assignableRoles(actor.role(), false, member.role()).contains(role)) {
            throw denied("Role " + role + " not assignable to this member");
        }
        if (member.role() == Role.ADMIN && member.isActive()) {
            ensureAnotherActiveAdmin(member);
        }
        var before = member.role();
        member.changeRole(role);
        passwordResets.cancelLinks(member.id());
        auditLog.record(
                actor,
                AuditAction.MEMBER_ROLE_CHANGED,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname(),
                before.name(),
                role.name());
        return detail(actor, member);
    }

    /**
     * The board accepts a new account (D-107): climber when licensed at the club, tourist
     * otherwise. Refusing it is deleting it ({@code AccountDeletionService#deleteMember}).
     *
     * @throws BusinessException {@code MEMBER_ALREADY_VALIDATED}, {@code VALIDATION_FAILED} (a role
     *     other than tourist or climber)
     */
    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public MemberDetailView validate(UUID actorId, UUID memberId, Role role) {
        var actor = load(actorId);
        var member = load(memberId);
        checkManageable(actor, member);
        member.validate(role, clock.instant());
        auditLog.record(
                actor,
                AuditAction.MEMBER_VALIDATED,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname(),
                null,
                role.name());
        return detail(actor, member);
    }

    /** Disabling closes every session of the member at once (spec §6). */
    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public MemberDetailView changeStatus(UUID actorId, UUID memberId, MemberStatus status) {
        var actor = load(actorId);
        var member = load(memberId);
        if (member.status() == status) {
            return detail(actor, member);
        }
        checkManageable(actor, member);
        if (status == MemberStatus.DISABLED) {
            if (member.role() == Role.ADMIN) {
                ensureAnotherActiveAdmin(member);
            }
            member.disable();
            devices.closeAll(member.id());
            passwordResets.cancelLinks(member.id());
        } else {
            member.enable();
        }
        auditLog.record(
                actor,
                status == MemberStatus.DISABLED
                        ? AuditAction.MEMBER_DISABLED
                        : AuditAction.MEMBER_ENABLED,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname(),
                null,
                null);
        return detail(actor, member);
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional
    public ResetLink createResetLink(UUID actorId, UUID memberId) {
        var actor = load(actorId);
        var member = load(memberId);
        checkManageable(actor, member);
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "Member is disabled");
        }
        var link = passwordResets.createLink(member.id(), actor.id());
        auditLog.record(
                actor,
                AuditAction.PASSWORD_RESET_LINK_CREATED,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname(),
                null,
                null);
        return link;
    }

    private MemberDetailView detail(Member actor, Member member) {
        var self = actor.id().equals(member.id());
        var manageable = MemberGovernance.canManageAccount(actor.role(), self, member.role());
        var validated = member.isValidated();
        var permissions =
                new Permissions(
                        validated
                                ? MemberGovernance.assignableRoles(
                                        actor.role(), self, member.role())
                                : Set.of(),
                        manageable && member.isActive(),
                        manageable && !member.isActive(),
                        manageable && member.isActive(),
                        MemberGovernance.canDelete(actor.role(), self, validated),
                        manageable && !validated);
        return new MemberDetailView(
                summary(member, sessions.countByMemberId(member.id())),
                member.email(),
                member.joinNote(),
                Age.needsParentalConsent(member.birthDate(), ClubTime.today(clock))
                        && member.hasParentalConsent(),
                member.clubArrivalYear(),
                member.climbingSinceYear(),
                permissions);
    }

    private static MemberSummaryView summary(Member member, long sessionCount) {
        return new MemberSummaryView(
                member.id(),
                member.nickname(),
                member.firstName(),
                member.lastName(),
                member.role(),
                member.status(),
                member.isValidated(),
                member.declaredLicensed(),
                member.avatarId(),
                member.createdAt(),
                member.lastSeenAt(),
                sessionCount);
    }

    private void checkManageable(Member actor, Member member) {
        checkNotSelf(actor, member);
        if (!MemberGovernance.canManageAccount(actor.role(), false, member.role())) {
            throw denied("Member out of reach for " + actor.role());
        }
    }

    private static void checkNotSelf(Member actor, Member member) {
        if (actor.id().equals(member.id())) {
            throw new BusinessException(
                    ErrorCode.SELF_MANAGEMENT_FORBIDDEN, "Members do not manage their own account");
        }
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
    private Member load(UUID memberId) {
        return members.findById(memberId)
                .filter(member -> !FormerMember.is(member.id()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    private static BusinessException denied(String message) {
        return new BusinessException(ErrorCode.ACCESS_DENIED, message);
    }
}
