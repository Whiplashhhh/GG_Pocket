package fr.gravelinesgrimp.pocket.topo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class RouteTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final Instant NOW = Instant.parse("2026-10-21T18:00:00Z");

    private final UUID yellow = UUID.randomUUID();
    private final UUID blue = UUID.randomUUID();

    @Test
    void aNewRouteIsOnTheWallWithItsColoursInOrder() {
        var route = route();

        assertThat(route.status()).isEqualTo(RouteStatus.ACTIVE);
        assertThat(route.holdColorIds()).containsExactly(blue, yellow);
        assertThat(route.styles()).containsExactly(RouteStyle.SLAB, RouteStyle.ROOF);
        assertThat(route.name()).isEqualTo("La dalle à Dédé");
        assertThat(route.setterNote()).isNull();
    }

    @Test
    void lastChanceComesAndGoes() {
        var route = route();

        route.announceLastChance(TODAY.plusDays(7), TODAY);
        assertThat(route.status()).isEqualTo(RouteStatus.LAST_CHANCE);
        assertThat(route.plannedDismountOn()).isEqualTo(TODAY.plusDays(7));

        route.announceLastChance(null, TODAY);
        assertThat(route.plannedDismountOn()).isNull();

        route.cancelLastChance();
        assertThat(route.status()).isEqualTo(RouteStatus.ACTIVE);
        assertRefused(route::cancelLastChance, ErrorCode.ROUTE_STATUS_CONFLICT);
    }

    @Test
    void theDismountCannotBePlannedInThePast() {
        var route = route();
        assertRefused(
                () -> route.announceLastChance(TODAY.minusDays(1), TODAY),
                ErrorCode.DATE_IN_THE_PAST);
        route.announceLastChance(TODAY, TODAY);
        assertThat(route.plannedDismountOn()).isEqualTo(TODAY);
    }

    @Test
    void aDismountedRouteIsReadOnlyUntilRemounted() {
        var route = route();
        route.announceLastChance(TODAY.plusDays(3), TODAY);

        route.dismount(NOW);
        assertThat(route.status()).isEqualTo(RouteStatus.DISMOUNTED);
        assertThat(route.dismountedAt()).isEqualTo(NOW);
        assertThat(route.plannedDismountOn()).isNull();
        assertRefused(() -> route.update(draft()), ErrorCode.ROUTE_DISMOUNTED);
        assertRefused(() -> route.changePhoto(UUID.randomUUID()), ErrorCode.ROUTE_DISMOUNTED);
        assertRefused(() -> route.announceLastChance(null, TODAY), ErrorCode.ROUTE_DISMOUNTED);
        assertRefused(route::cancelLastChance, ErrorCode.ROUTE_DISMOUNTED);
        assertRefused(() -> route.dismount(NOW), ErrorCode.ROUTE_STATUS_CONFLICT);

        route.remount();
        assertThat(route.status()).isEqualTo(RouteStatus.ACTIVE);
        assertThat(route.dismountedAt()).isNull();
        assertRefused(route::remount, ErrorCode.ROUTE_STATUS_CONFLICT);
    }

    @Test
    void anEditReplacesSettersAndStyles() {
        var route = route();
        var setter = UUID.randomUUID();
        route.update(
                new RouteDraft(
                        "  Le toit qui pique ",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        List.of(yellow),
                        TODAY,
                        List.of(setter),
                        " Un stagiaire ",
                        "  ",
                        List.of()));

        assertThat(route.name()).isEqualTo("Le toit qui pique");
        assertThat(route.holdColorIds()).containsExactly(yellow);
        assertThat(route.setterIds()).containsExactly(setter);
        assertThat(route.setterNote()).isEqualTo("Un stagiaire");
        assertThat(route.advice()).isNull();
        assertThat(route.styles()).isEmpty();
    }

    private Route route() {
        return new Route(draft(), UUID.randomUUID());
    }

    private RouteDraft draft() {
        return new RouteDraft(
                "La dalle à Dédé",
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(blue, yellow),
                TODAY,
                List.of(UUID.randomUUID()),
                null,
                "Fais confiance à tes pieds.",
                List.of(RouteStyle.ROOF, RouteStyle.SLAB));
    }

    private static void assertRefused(ThrowingCallable action, ErrorCode code) {
        assertThatThrownBy(action)
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
