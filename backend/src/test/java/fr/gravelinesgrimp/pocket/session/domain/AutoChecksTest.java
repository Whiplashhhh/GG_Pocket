package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.topo.domain.CheckSource;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

class AutoChecksTest {

    private static final RouteMode LEAD = new RouteMode(UUID.randomUUID(), ClimbMode.LEAD);
    private static final RouteMode TOP_ROPE = new RouteMode(UUID.randomUUID(), ClimbMode.TOP_ROPE);

    @Test
    void aCleanAscentMarksTheRouteInItsMode() {
        var plan = AutoChecks.plan(Set.of(LEAD, TOP_ROPE), Set.of(LEAD), Map.of());
        assertThat(plan.toCreate()).containsExactly(LEAD);
        assertThat(plan.toDelete()).isEmpty();
    }

    @Test
    void aMarkAlreadyThereIsKeptWhateverItsSource() {
        var plan =
                AutoChecks.plan(
                        Set.of(LEAD, TOP_ROPE),
                        Set.of(LEAD, TOP_ROPE),
                        Map.of(LEAD, CheckSource.MANUAL, TOP_ROPE, CheckSource.ASCENT));
        assertThat(plan.toCreate()).isEmpty();
        assertThat(plan.toDelete()).isEmpty();
    }

    @Test
    void withoutACleanAscentOnlyTheDeducedMarkGoes() {
        var plan =
                AutoChecks.plan(
                        Set.of(LEAD, TOP_ROPE),
                        Set.of(),
                        Map.of(LEAD, CheckSource.ASCENT, TOP_ROPE, CheckSource.MANUAL));
        assertThat(plan.toCreate()).isEmpty();
        assertThat(plan.toDelete()).containsExactly(LEAD);
    }

    @Test
    void pairsThatWereNotTouchedAreLeftAlone() {
        var plan =
                AutoChecks.plan(Set.of(TOP_ROPE), Set.of(LEAD), Map.of(LEAD, CheckSource.ASCENT));
        assertThat(plan.toCreate()).isEmpty();
        assertThat(plan.toDelete()).isEmpty();
    }
}
