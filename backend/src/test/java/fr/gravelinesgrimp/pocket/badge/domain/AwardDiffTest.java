package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class AwardDiffTest {

    private final UUID monday = UUID.randomUUID();
    private final UUID wednesday = UUID.randomUUID();
    private final LocalDate day = LocalDate.parse("2025-03-05");

    @Test
    void newAwardsAreAdded() {
        var week = new Award(BadgeCode.WEEK_2, "2025-03-03", wednesday, day);

        var diff = AwardDiff.between(List.of(), List.of(week));

        assertThat(diff.added()).containsExactly(week);
        assertThat(diff.removed()).isEmpty();
        assertThat(diff.moved()).isEmpty();
    }

    @Test
    void awardsNoLongerEarnedAreRemoved() {
        var first = new Award(BadgeCode.TOPPED_1, "", monday, day);

        var diff = AwardDiff.between(List.of(first), List.of());

        assertThat(diff.removed()).containsExactly(first.id());
        assertThat(diff.added()).isEmpty();
    }

    @Test
    void anAwardEarnedAgainByAnotherSessionMoves() {
        var before = new Award(BadgeCode.TOPPED_1, "", monday, day.minusDays(2));
        var after = new Award(BadgeCode.TOPPED_1, "", wednesday, day);

        var diff = AwardDiff.between(List.of(before), List.of(after));

        assertThat(diff.moved()).containsExactly(after);
        assertThat(diff.added()).isEmpty();
        assertThat(diff.removed()).isEmpty();
    }

    @Test
    void theSameAwardsChangeNothing() {
        var award = new Award(BadgeCode.TOPPED_1, "", monday, day);

        assertThat(AwardDiff.between(List.of(award), List.of(award)).isEmpty()).isTrue();
    }
}
