package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditEntry;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.admin.infra.AuditEntryRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Writes the audit log, inside the transaction of the change it records: both are saved, or
 * neither.
 */
@Component
public class AuditLog {

    private final AuditEntryRepository entries;
    private final Clock clock;

    public AuditLog(AuditEntryRepository entries, Clock clock) {
        this.entries = entries;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            Member actor,
            AuditAction action,
            AuditTargetType targetType,
            UUID targetId,
            String targetLabel,
            String before,
            String after) {
        entries.save(
                new AuditEntry(
                        clock.instant(),
                        actor.id(),
                        actor.nickname(),
                        action,
                        targetType,
                        targetId,
                        targetLabel,
                        before,
                        after));
    }
}
