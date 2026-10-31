package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BadgeTest {

    @Test
    void aTierFamilyIsOneBadgeWithItsTiers() {
        assertThat(Badge.TOPPED.codes()).isEqualTo(BadgeCode.tiers(BadgeFamily.TOPPED));
        assertThat(Badge.FIRST_GRADE.codes()).isEqualTo(BadgeCode.tiers(BadgeFamily.FIRST_GRADE));
        assertThat(Badge.of(BadgeCode.DAYS_750)).isEqualTo(Badge.CLIMBING_DAYS);
        assertThat(Badge.LEAD.tiered()).isTrue();
    }

    @Test
    void aRepeatableBadgeIsItsOwnCode() {
        assertThat(Badge.of(BadgeCode.FLASH)).isEqualTo(Badge.FLASH);
        assertThat(Badge.WEEK_3.codes()).containsExactly(BadgeCode.WEEK_3);
        assertThat(Badge.RECORD.tiered()).isFalse();
    }

    @Test
    void everyCodeBelongsToExactlyOneBadge() {
        assertThat(Arrays.stream(Badge.values()).flatMap(badge -> badge.codes().stream()))
                .containsExactly(BadgeCode.values());
        assertThat(Badge.values()).hasSize(17);
    }
}
