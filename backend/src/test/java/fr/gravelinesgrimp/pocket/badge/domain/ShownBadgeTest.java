package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ShownBadgeTest {

    private static final Map<BadgeCode, Long> COUNTS =
            Map.of(
                    BadgeCode.TOPPED_1, 1L,
                    BadgeCode.TOPPED_5, 1L,
                    BadgeCode.TOPPED_10, 1L,
                    BadgeCode.GRADE_5B, 1L,
                    BadgeCode.FLASH, 4L);

    @Test
    void theLevelOfATierFamilyIsItsEarnedTiers() {
        assertThat(ShownBadge.of(Badge.TOPPED, COUNTS)).isEqualTo(new ShownBadge(Badge.TOPPED, 3));
        assertThat(ShownBadge.of(Badge.FIRST_GRADE, COUNTS).level()).isEqualTo(1);
    }

    @Test
    void theLevelOfARepeatableBadgeIsItsCount() {
        assertThat(ShownBadge.of(Badge.FLASH, COUNTS)).isEqualTo(new ShownBadge(Badge.FLASH, 4));
    }

    @Test
    void aBadgeNotEarnedIsLevelZero() {
        assertThat(ShownBadge.of(Badge.LEAD, COUNTS).level()).isZero();
        assertThat(ShownBadge.of(Badge.COMEBACK, COUNTS).level()).isZero();
    }

    @Test
    void onlyTheEarnedFavouritesAreShownInTheirOrder() {
        assertThat(ShownBadge.earned(List.of(Badge.FLASH, Badge.LEAD, Badge.TOPPED), COUNTS))
                .containsExactly(new ShownBadge(Badge.FLASH, 4), new ShownBadge(Badge.TOPPED, 3));
    }
}
