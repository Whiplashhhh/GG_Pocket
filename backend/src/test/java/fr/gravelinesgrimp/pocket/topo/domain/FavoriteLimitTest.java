package fr.gravelinesgrimp.pocket.topo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.FavoriteLimit.Decision;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class FavoriteLimitTest {

    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    private final UUID third = UUID.randomUUID();

    @Test
    void addsWhileTheHeartHasRoom() {
        assertThat(FavoriteLimit.decide(List.of(), first, null)).isEqualTo(Decision.ADD);
        assertThat(FavoriteLimit.decide(List.of(first), second, null)).isEqualTo(Decision.ADD);
    }

    @Test
    void heartingTwiceChangesNothing() {
        assertThat(FavoriteLimit.decide(List.of(first, second), first, null))
                .isEqualTo(Decision.ALREADY_THERE);
    }

    @Test
    void aFullHeartRefusesAThirdOne() {
        assertThatThrownBy(() -> FavoriteLimit.decide(List.of(first, second), third, null))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        e -> assertThat(e.code()).isEqualTo(ErrorCode.FAVORITES_FULL));
    }

    @Test
    void aFullHeartSwapsOneOfItsOwnRoutes() {
        assertThat(FavoriteLimit.decide(List.of(first, second), third, second))
                .isEqualTo(Decision.REPLACE);
        // Giving up a route that is not one of my hearts does not make room.
        assertThatThrownBy(
                        () ->
                                FavoriteLimit.decide(
                                        List.of(first, second), third, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void someoneBackAtThreeAfterARemountMustGiveOneUp() {
        var three = List.of(first, second, third);
        var fourth = UUID.randomUUID();
        assertThatThrownBy(() -> FavoriteLimit.decide(three, fourth, null))
                .isInstanceOf(BusinessException.class);
        assertThat(FavoriteLimit.decide(three, fourth, first)).isEqualTo(Decision.REPLACE);
    }
}
