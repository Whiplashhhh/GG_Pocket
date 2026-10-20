package fr.gravelinesgrimp.pocket.admin.application;

import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditEntry;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.admin.infra.AuditEntryRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/** Reading the audit log: board and admin (spec §5). */
@Service
public class AuditQueryService {

    public static final int MAX_PAGE_SIZE = 100;

    public record AuditEntryView(
            UUID id,
            Instant occurredAt,
            UUID actorId,
            String actorNickname,
            AuditAction action,
            AuditTargetType targetType,
            UUID targetId,
            String targetLabel,
            String before,
            String after) {}

    /** {@code nextCursor} is null on the last page. */
    public record AuditPage(List<AuditEntryView> entries, String nextCursor) {}

    private final AuditEntryRepository entries;

    public AuditQueryService(AuditEntryRepository entries) {
        this.entries = entries;
    }

    @PreAuthorize("hasRole('BOARD')")
    @Transactional(readOnly = true)
    public AuditPage page(String cursor, int size) {
        var limit = Math.clamp(size, 1, MAX_PAGE_SIZE);
        // One more than asked: tells whether another page exists.
        List<AuditEntry> found;
        if (cursor == null || cursor.isBlank()) {
            found = entries.findLatest(limit + 1);
        } else {
            var position = Cursor.decode(cursor);
            found = entries.findBefore(position.occurredAt(), position.id(), limit + 1);
        }
        var page = found.stream().limit(limit).map(AuditQueryService::toView).toList();
        var next =
                found.size() > limit
                        ? Cursor.encode(page.getLast().occurredAt(), page.getLast().id())
                        : null;
        return new AuditPage(page, next);
    }

    private static AuditEntryView toView(AuditEntry entry) {
        return new AuditEntryView(
                entry.getId(),
                entry.occurredAt(),
                entry.actorId(),
                entry.actorNickname(),
                entry.action(),
                entry.targetType(),
                entry.targetId(),
                entry.targetLabel(),
                entry.beforeValue(),
                entry.afterValue());
    }

    /** Opaque to clients: the date and id of the last entry already shown. */
    record Cursor(Instant occurredAt, UUID id) {

        static String encode(Instant occurredAt, UUID id) {
            var raw = occurredAt + "|" + id;
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String cursor) {
            try {
                var raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                var parts = raw.split("\\|", 2);
                return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
            } catch (RuntimeException e) {
                throw new BusinessException(ErrorCode.MALFORMED_REQUEST, "Bad audit cursor");
            }
        }
    }
}
