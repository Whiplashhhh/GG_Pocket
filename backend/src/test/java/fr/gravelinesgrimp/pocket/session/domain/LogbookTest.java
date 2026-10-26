package fr.gravelinesgrimp.pocket.session.domain;

import static fr.gravelinesgrimp.pocket.topo.domain.ClimbMode.LEAD;
import static fr.gravelinesgrimp.pocket.topo.domain.ClimbMode.TOP_ROPE;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import fr.gravelinesgrimp.pocket.session.domain.Logbook.Grade;
import fr.gravelinesgrimp.pocket.session.domain.Logbook.GradeCount;
import fr.gravelinesgrimp.pocket.session.domain.Logbook.RouteLine;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class LogbookTest {

    private static final Grade SIX_A = new Grade("6a", 20);
    private static final Grade SIX_B = new Grade("6b", 22);
    private static final Grade SEVEN_A = new Grade("7a", 31);
    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    private final UUID easy = UUID.randomUUID();
    private final UUID medium = UUID.randomUUID();
    private final UUID hard = UUID.randomUUID();
    private final Map<UUID, Grade> grades = Map.of(easy, SIX_A, medium, SIX_B, hard, SEVEN_A);
    private final UUID monday = UUID.randomUUID();
    private final UUID mondayAgain = UUID.randomUUID();
    private final UUID friday = UUID.randomUUID();

    @Test
    void anEmptyLogbook() {
        var logbook = Logbook.of(List.of(), grades::get);
        assertThat(logbook.totals().sessionCount()).isZero();
        assertThat(logbook.totals().hardestTopped()).isEmpty();
        assertThat(logbook.grades()).isEmpty();
        assertThat(logbook.routes()).isEmpty();
    }

    @Test
    void countsAscentsWithRepeatsAndSessionsAndDays() {
        var logbook = Logbook.of(sample(), grades::get);
        var totals = logbook.totals();
        assertThat(totals.sessionCount()).isEqualTo(3);
        assertThat(totals.climbingDays()).isEqualTo(2);
        assertThat(totals.ascentCount()).isEqualTo(6);
        assertThat(totals.toppedCount()).isEqualTo(4);
        assertThat(totals.leadToppedCount()).isEqualTo(2);
        assertThat(totals.cleanCount()).isEqualTo(2);
        assertThat(totals.routeCount()).isEqualTo(3);
        assertThat(totals.hardestTopped()).contains(SIX_B);
        assertThat(totals.hardestLeadTopped()).contains(SIX_A);
    }

    @Test
    void keepsTheBestAscentOfEachRoute() {
        var routes = Logbook.of(sample(), grades::get).routes();
        assertThat(routes)
                .extracting(RouteLine::routeId, RouteLine::best)
                .containsExactly(
                        tuple(hard, AscentBest.TRIED),
                        tuple(medium, AscentBest.TOP_ROPE_CLEAN),
                        tuple(easy, AscentBest.LEAD_CLEAN));
        var easyLine = routes.getLast();
        assertThat(easyLine.ascentCount()).isEqualTo(3);
        assertThat(easyLine.toppedCount()).isEqualTo(3);
        assertThat(easyLine.firstClimbedOn()).isEqualTo(DAY);
        assertThat(easyLine.lastClimbedOn()).isEqualTo(DAY);
    }

    @Test
    void thePyramidCountsDistinctToppedRoutesByTheModeOfTheirBest() {
        var pyramid = Logbook.of(sample(), grades::get).grades();
        assertThat(pyramid)
                .containsExactly(new GradeCount(SIX_B, 1, 0), new GradeCount(SIX_A, 0, 1));
    }

    @Test
    void theBestIsCleanLeadThenToppedLeadThenTopRope() {
        assertThat(AscentBest.of(LEAD, true, true)).isEqualTo(AscentBest.LEAD_CLEAN);
        assertThat(AscentBest.of(LEAD, true, false)).isEqualTo(AscentBest.LEAD_TOPPED);
        assertThat(AscentBest.of(TOP_ROPE, true, true)).isEqualTo(AscentBest.TOP_ROPE_CLEAN);
        assertThat(AscentBest.of(TOP_ROPE, true, false)).isEqualTo(AscentBest.TOP_ROPE_TOPPED);
        assertThat(AscentBest.of(LEAD, false, false)).isEqualTo(AscentBest.TRIED);
        assertThat(AscentBest.LEAD_TOPPED.bestWith(AscentBest.TOP_ROPE_CLEAN))
                .isEqualTo(AscentBest.LEAD_TOPPED);
        assertThat(AscentBest.TRIED.bestWith(AscentBest.TOP_ROPE_TOPPED))
                .isEqualTo(AscentBest.TOP_ROPE_TOPPED);
    }

    /**
     * Two sessions on Monday the 12th, one on Friday the 16th: the easy route three times (clean in
     * lead once), the medium one clean in top rope after a fall, the hard one only tried.
     */
    private List<MyAscentLine> sample() {
        return List.of(
                line(monday, DAY, easy, TOP_ROPE, true, false),
                line(monday, DAY, easy, LEAD, true, true),
                line(mondayAgain, DAY, easy, LEAD, true, false),
                line(friday, DAY.plusDays(4), medium, TOP_ROPE, false, false),
                line(friday, DAY.plusDays(4), medium, TOP_ROPE, true, true),
                line(friday, DAY.plusDays(4), hard, LEAD, false, false));
    }

    private static MyAscentLine line(
            UUID session,
            LocalDate day,
            UUID route,
            ClimbMode mode,
            boolean topped,
            boolean clean) {
        return new MyAscentLine(session, day, route, mode, topped, clean);
    }
}
