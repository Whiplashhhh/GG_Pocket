package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.session.domain.SessionTotals.Line;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.util.List;

class SessionTotalsTest {

    @Test
    void countsAscentsToppedOnesAndLeadAndKeepsTheHardestTopped() {
        var totals =
                SessionTotals.of(
                        List.of(
                                new Line(ClimbMode.LEAD, true, "6a", 20),
                                new Line(ClimbMode.TOP_ROPE, true, "6b+", 25),
                                new Line(ClimbMode.LEAD, false, "7a", 31),
                                new Line(ClimbMode.TOP_ROPE, true, "6a", 20)));
        assertThat(totals.ascentCount()).isEqualTo(4);
        assertThat(totals.toppedCount()).isEqualTo(3);
        assertThat(totals.leadCount()).isEqualTo(2);
        assertThat(totals.hardestTopped()).map(Line::grade).contains("6b+");
    }

    @Test
    void nothingToppedMeansNoHardestGrade() {
        var totals = SessionTotals.of(List.of(new Line(ClimbMode.LEAD, false, "7a", 31)));
        assertThat(totals.toppedCount()).isZero();
        assertThat(totals.hardestTopped()).isEmpty();
    }
}
