package fr.gravelinesgrimp.pocket.session.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.session.application.SessionService.Cursor;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

class SessionCursorTest {

    @Test
    void aCursorGivesBackWhatItWasMadeOf() {
        var day = LocalDate.parse("2026-10-23");
        var savedAt = Instant.parse("2026-10-23T18:04:05.123456Z");
        var id = UUID.randomUUID();
        assertThat(Cursor.decode(Cursor.encode(day, savedAt, id)))
                .isEqualTo(new Cursor(day, savedAt, id));
    }

    @Test
    void aForgedCursorIsAMalformedRequest() {
        assertThatThrownBy(() -> Cursor.decode("pas-un-curseur"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.MALFORMED_REQUEST));
    }
}
