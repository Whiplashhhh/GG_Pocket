package fr.gravelinesgrimp.pocket.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.admin.application.AuditQueryService.Cursor;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class AuditCursorTest {

    @Test
    void roundTrips() {
        var at = Instant.parse("2026-10-20T10:15:30.123456Z");
        var id = UUID.randomUUID();
        assertThat(Cursor.decode(Cursor.encode(at, id))).isEqualTo(new Cursor(at, id));
    }

    @Test
    void aTamperedCursorIsAMalformedRequest() {
        assertThatThrownBy(() -> Cursor.decode("pas-un-curseur"))
                .isInstanceOf(BusinessException.class);
    }
}
