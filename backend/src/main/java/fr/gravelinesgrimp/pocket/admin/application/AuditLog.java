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
 * neither. Texts longer than their column (a comment body, a list of sectors…) are shortened.
 */
@Component
public class AuditLog {

    static final int LABEL_LENGTH = 100;
    static final int VALUE_LENGTH = 200;

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
                        shorten(targetLabel, LABEL_LENGTH),
                        shorten(before, VALUE_LENGTH),
                        shorten(after, VALUE_LENGTH)));
    }

    static String shorten(String text, int length) {
        if (text == null || text.length() <= length) {
            return text;
        }
        var end = length - 1;
        // Never cut an emoji in half.
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end) + "…";
    }
}
