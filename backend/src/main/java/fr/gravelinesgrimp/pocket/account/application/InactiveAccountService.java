package fr.gravelinesgrimp.pocket.account.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.auth.application.DeviceService;
import fr.gravelinesgrimp.pocket.auth.application.PasswordResetService;
import fr.gravelinesgrimp.pocket.member.domain.FormerMember;
import fr.gravelinesgrimp.pocket.member.domain.Inactivity;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Puts an account to sleep after a year without a visit (D-119): disabled like the board would,
 * sessions closed and reset links cancelled, but marked as asleep so that the member is told why at
 * sign-in and the board sees it on the sheet. Deleting after two years is {@link
 * AccountDeletionService#deleteIfInactive}.
 */
@Service
public class InactiveAccountService {

    private final MemberRepository members;
    private final DeviceService devices;
    private final PasswordResetService passwordResets;
    private final AuditLog auditLog;
    private final Clock clock;

    public InactiveAccountService(
            MemberRepository members,
            DeviceService devices,
            PasswordResetService passwordResets,
            AuditLog auditLog,
            Clock clock) {
        this.members = members;
        this.devices = devices;
        this.passwordResets = passwordResets;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    /**
     * Checks again, under lock, that the account is still that inactive (never an admin).
     *
     * @return whether the account was put to sleep
     */
    @Transactional
    public boolean putToSleepIfInactive(UUID memberId) {
        var member =
                members.lockById(memberId)
                        .filter(found -> !FormerMember.is(found.id()))
                        .orElse(null);
        var now = clock.instant();
        if (member == null || member.inactivity(now) != Inactivity.Verdict.PUT_TO_SLEEP) {
            return false;
        }
        member.fallAsleep(now);
        devices.closeAll(member.id());
        passwordResets.cancelLinks(member.id());
        auditLog.recordBySystem(
                AuditAction.MEMBER_PUT_TO_SLEEP,
                AuditTargetType.MEMBER,
                member.id(),
                member.nickname());
        return true;
    }
}
