package fr.gravelinesgrimp.pocket.club.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class SectorOrderingTest {

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    @Test
    void acceptsEverySectorOnceInAnyOrder() {
        assertThatCode(() -> SectorOrdering.check(List.of(c, a, b), List.of(a, b, c)))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesMissingRepeatedOrUnknownSectors() {
        assertThatThrownBy(() -> SectorOrdering.check(List.of(a, b), List.of(a, b, c)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> SectorOrdering.check(List.of(a, a, b), List.of(a, b, c)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(
                        () ->
                                SectorOrdering.check(
                                        List.of(a, b, UUID.randomUUID()), List.of(a, b, c)))
                .isInstanceOf(BusinessException.class);
    }
}
