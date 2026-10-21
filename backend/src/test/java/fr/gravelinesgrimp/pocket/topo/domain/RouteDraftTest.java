package fr.gravelinesgrimp.pocket.topo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class RouteDraftTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final UUID RED = UUID.randomUUID();
    private static final UUID BLACK = UUID.randomUUID();
    private static final UUID SETTER = UUID.randomUUID();

    @Test
    void aCompleteDraftPasses() {
        assertThatCode(() -> draft(List.of(RED, BLACK), TODAY, List.of(SETTER), null).check(TODAY))
                .doesNotThrowAnyException();
    }

    @Test
    void aSetterWithoutAccountIsEnough() {
        assertThatCode(() -> draft(List.of(RED), TODAY, List.of(), "Le cousin").check(TODAY))
                .doesNotThrowAnyException();
    }

    @Test
    void namesAtLeastOneSetter() {
        assertRefused(draft(List.of(RED), TODAY, List.of(), null), ErrorCode.INVALID_SETTERS);
        assertRefused(draft(List.of(RED), TODAY, List.of(), "   "), ErrorCode.INVALID_SETTERS);
    }

    @Test
    void takesOneOrTwoDistinctColours() {
        assertRefused(draft(List.of(), TODAY, List.of(SETTER), null), ErrorCode.VALIDATION_FAILED);
        assertRefused(
                draft(List.of(RED, RED), TODAY, List.of(SETTER), null),
                ErrorCode.VALIDATION_FAILED);
        assertRefused(
                draft(List.of(RED, BLACK, UUID.randomUUID()), TODAY, List.of(SETTER), null),
                ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void opensTodayAtTheLatest() {
        assertRefused(
                draft(List.of(RED), TODAY.plusDays(1), List.of(SETTER), null),
                ErrorCode.DATE_IN_THE_FUTURE);
    }

    @Test
    void takesAtMostFiveDistinctStyles() {
        var six =
                List.of(
                        RouteStyle.SLAB,
                        RouteStyle.ROOF,
                        RouteStyle.JUGS,
                        RouteStyle.CRIMPS,
                        RouteStyle.PINCHES,
                        RouteStyle.REACHY);
        assertRefused(withStyles(six), ErrorCode.VALIDATION_FAILED);
        assertRefused(
                withStyles(List.of(RouteStyle.SLAB, RouteStyle.SLAB)), ErrorCode.VALIDATION_FAILED);
        assertThatCode(() -> withStyles(six.subList(0, 5)).check(TODAY)).doesNotThrowAnyException();
    }

    @Test
    void refusesTooLongTexts() {
        var longName =
                new RouteDraft(
                        "x".repeat(61),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(RED),
                        TODAY,
                        List.of(SETTER),
                        null,
                        null,
                        List.of());
        assertRefused(longName, ErrorCode.VALIDATION_FAILED);
        assertRefused(
                draft(List.of(RED), TODAY, List.of(), "y".repeat(61)), ErrorCode.VALIDATION_FAILED);
    }

    private static RouteDraft draft(
            List<UUID> colors, LocalDate openedOn, List<UUID> setters, String setterNote) {
        return new RouteDraft(
                "Gaston la gaffe",
                UUID.randomUUID(),
                UUID.randomUUID(),
                colors,
                openedOn,
                setters,
                setterNote,
                null,
                List.of(RouteStyle.TECHNICAL));
    }

    private static RouteDraft withStyles(List<RouteStyle> styles) {
        return new RouteDraft(
                "Gaston la gaffe",
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(RED),
                TODAY,
                List.of(SETTER),
                null,
                null,
                styles);
    }

    private static void assertRefused(RouteDraft draft, ErrorCode code) {
        assertThatThrownBy(() -> draft.check(TODAY))
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
