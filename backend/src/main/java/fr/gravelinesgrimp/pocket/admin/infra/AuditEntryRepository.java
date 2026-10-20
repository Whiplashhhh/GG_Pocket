package fr.gravelinesgrimp.pocket.admin.infra;

import fr.gravelinesgrimp.pocket.admin.domain.AuditEntry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, UUID> {

    @Query(
            value = "select * from audit_entry order by occurred_at desc, id desc limit :limit",
            nativeQuery = true)
    List<AuditEntry> findLatest(int limit);

    /** The page after a cursor, newest first (index {@code audit_entry_occurred_idx}). */
    @Query(
            value =
                    "select * from audit_entry where (occurred_at, id) < (:occurredAt, :id)"
                            + " order by occurred_at desc, id desc limit :limit",
            nativeQuery = true)
    List<AuditEntry> findBefore(Instant occurredAt, UUID id, int limit);
}
