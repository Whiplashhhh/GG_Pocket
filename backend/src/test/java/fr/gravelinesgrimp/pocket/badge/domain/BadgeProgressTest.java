package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

class BadgeProgressTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    @Test
    void countsTheToppedAscentsTheLeadOnesAndTheClimbingDays() {
        var history =
                List.of(
                        session(DAY, line(ClimbMode.LEAD, true), line(ClimbMode.TOP_ROPE, true)),
                        session(DAY, line(ClimbMode.LEAD, false)),
                        session(DAY.plusDays(2), line(ClimbMode.LEAD, true)));

        assertThat(BadgeProgress.of(history)).isEqualTo(new BadgeProgress(3, 2, 2));
    }

    @Test
    void startsFromNothing() {
        assertThat(BadgeProgress.of(List.of())).isEqualTo(new BadgeProgress(0, 0, 0));
    }

    private static ClimbRecord session(LocalDate day, ClimbRecord.Line... lines) {
        return new ClimbRecord(UUID.randomUUID(), day, Instant.EPOCH, List.of(lines));
    }

    private static ClimbRecord.Line line(ClimbMode mode, boolean topped) {
        return new ClimbRecord.Line(UUID.randomUUID(), mode, topped, topped, false, "6a", DAY);
    }
}
