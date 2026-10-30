package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BadgeCodeTest {

    @Test
    void tiersFollowTheSpec() {
        assertThat(BadgeCode.tiers(BadgeFamily.TOPPED))
                .extracting(BadgeCode::threshold)
                .containsExactly(1, 5, 10, 20, 50, 100, 250, 500, 750, 1000);
        assertThat(BadgeCode.tiers(BadgeFamily.LEAD))
                .extracting(BadgeCode::threshold)
                .containsExactly(1, 5, 10, 20, 50, 100, 250, 500, 750, 1000);
        assertThat(BadgeCode.tiers(BadgeFamily.FIRST_GRADE))
                .extracting(BadgeCode::grade)
                .containsExactly("5b", "5c", "6a", "6b", "6c", "7a", "7b", "7c");
        assertThat(BadgeCode.tiers(BadgeFamily.CLIMBING_DAYS))
                .extracting(BadgeCode::threshold)
                .containsExactly(10, 25, 50, 75, 100, 500, 750, 1000);
    }

    @Test
    void onlyTheRepeatableFamilyRepeats() {
        assertThat(Arrays.stream(BadgeCode.values()).filter(BadgeCode::repeatable))
                .containsExactly(
                        BadgeCode.WEEK_2,
                        BadgeCode.WEEK_3,
                        BadgeCode.WEEK_4,
                        BadgeCode.MONTH_COMPLETE,
                        BadgeCode.MONTH_12,
                        BadgeCode.STREAK_3W,
                        BadgeCode.MONTH_NO_MISS,
                        BadgeCode.FULL_LEAD,
                        BadgeCode.PERFECT,
                        BadgeCode.ZERO_REST,
                        BadgeCode.COMEBACK,
                        BadgeCode.FLASH,
                        BadgeCode.RECORD);
    }

    @Test
    void codesFitTheirColumn() {
        assertThat(BadgeCode.values()).allMatch(code -> code.name().length() <= 32);
    }
}
