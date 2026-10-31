package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCode;
import fr.gravelinesgrimp.pocket.auth.domain.RegistrationCodeFormat;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Registration codes, managed by the admin only (spec §5). The first-start admin code is not one of
 * them: it never shows up here.
 */
@Service
public class RegistrationCodeAdminService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public record RegistrationCodeView(
            UUID id,
            String code,
            String label,
            Instant expiresAt,
            Integer maxUses,
            int useCount,
            boolean active,
            RegistrationCode.State state,
            Instant createdAt) {}

    /** What the admin sets on a code; null expiry or maximum means "no limit". */
    public record CodeSettings(String label, Instant expiresAt, Integer maxUses, boolean active) {}

    private final RegistrationCodeRepository codes;
    private final MemberRepository members;
    private final AuditLog auditLog;
    private final Clock clock;

    public RegistrationCodeAdminService(
            RegistrationCodeRepository codes,
            MemberRepository members,
            AuditLog auditLog,
            Clock clock) {
        this.codes = codes;
        this.members = members;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<RegistrationCodeView> list() {
        var now = clock.instant();
        return codes.findAllByGrantedRoleOrderByCreatedAtDesc(Role.TOURIST).stream()
                .map(code -> toView(code, now))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RegistrationCodeView create(UUID actorId, CodeSettings settings) {
        var actor = load(actorId);
        String value;
        do {
            value = RegistrationCodeFormat.generate(RANDOM);
        } while (codes.existsByCode(value));
        var code =
                codes.save(
                        new RegistrationCode(
                                value,
                                settings.label(),
                                Role.TOURIST,
                                settings.expiresAt(),
                                settings.maxUses(),
                                actor.id()));
        if (!settings.active()) {
            code.deactivate();
        }
        auditLog.record(
                actor,
                AuditAction.REGISTRATION_CODE_CREATED,
                AuditTargetType.REGISTRATION_CODE,
                code.id(),
                code.label(),
                null,
                describe(code));
        return toView(code, clock.instant());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RegistrationCodeView update(UUID actorId, UUID codeId, CodeSettings settings) {
        var actor = load(actorId);
        var code =
                codes.findById(codeId)
                        .filter(found -> found.grantedRole() == Role.TOURIST)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No code"));
        var before = describe(code);
        code.update(settings.label(), settings.expiresAt(), settings.maxUses(), settings.active());
        auditLog.record(
                actor,
                AuditAction.REGISTRATION_CODE_UPDATED,
                AuditTargetType.REGISTRATION_CODE,
                code.id(),
                code.label(),
                before,
                describe(code));
        return toView(code, clock.instant());
    }

    /** Short, stable text for the audit log ("active, expires …, 12/30 uses"). */
    private static String describe(RegistrationCode code) {
        return (code.active() ? "active" : "inactive")
                + ", expires "
                + (code.expiresAt() == null ? "never" : code.expiresAt().toString())
                + ", "
                + code.useCount()
                + "/"
                + (code.maxUses() == null ? "unlimited" : code.maxUses().toString())
                + " uses";
    }

    private static RegistrationCodeView toView(RegistrationCode code, Instant now) {
        return new RegistrationCodeView(
                code.id(),
                RegistrationCodeFormat.display(code.code()),
                code.label(),
                code.expiresAt(),
                code.maxUses(),
                code.useCount(),
                code.active(),
                code.state(now),
                code.createdAt());
    }

    private Member load(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }
}
