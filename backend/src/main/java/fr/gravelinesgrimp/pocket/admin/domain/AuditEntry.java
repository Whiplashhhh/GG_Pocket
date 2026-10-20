package fr.gravelinesgrimp.pocket.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

/**
 * One line of the audit log: who did what, to whom, when, with the value before and after.
 * Append-only: written once, never updated.
 */
@Entity
@Table(name = "audit_entry")
public class AuditEntry implements Persistable<UUID> {

    @Id private UUID id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Column(name = "actor_nickname", updatable = false, length = 20)
    private String actorNickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false, length = 30)
    private AuditTargetType targetType;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @Column(name = "target_label", nullable = false, updatable = false, length = 100)
    private String targetLabel;

    @Column(name = "before_value", updatable = false, length = 200)
    private String beforeValue;

    @Column(name = "after_value", updatable = false, length = 200)
    private String afterValue;

    /** Entries are only ever inserted: tells Spring Data not to look for an existing row. */
    @Transient private boolean isNew = true;

    protected AuditEntry() {
        // for JPA
    }

    public AuditEntry(
            Instant occurredAt,
            UUID actorId,
            String actorNickname,
            AuditAction action,
            AuditTargetType targetType,
            UUID targetId,
            String targetLabel,
            String beforeValue,
            String afterValue) {
        this.id = UUID.randomUUID();
        this.occurredAt = occurredAt;
        this.actorId = actorId;
        this.actorNickname = actorNickname;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetLabel = targetLabel;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public UUID actorId() {
        return actorId;
    }

    public String actorNickname() {
        return actorNickname;
    }

    public AuditAction action() {
        return action;
    }

    public AuditTargetType targetType() {
        return targetType;
    }

    public UUID targetId() {
        return targetId;
    }

    public String targetLabel() {
        return targetLabel;
    }

    public String beforeValue() {
        return beforeValue;
    }

    public String afterValue() {
        return afterValue;
    }
}
