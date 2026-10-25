package fr.gravelinesgrimp.pocket.topo.domain;

import static fr.gravelinesgrimp.pocket.topo.domain.ClimbMode.LEAD;
import static fr.gravelinesgrimp.pocket.topo.domain.ClimbMode.TOP_ROPE;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Set;

class TodoStatesTest {

    @Test
    void nothingKnownLeavesTheRouteToDoInBothModes() {
        assertThat(TodoStates.of(Set.of(), Set.of()))
                .isEqualTo(new TodoStates(TodoState.TODO, TodoState.TODO));
    }

    @Test
    void aMarkByHandIsChecked() {
        assertThat(TodoStates.of(Set.of(TOP_ROPE), Set.of()))
                .isEqualTo(new TodoStates(TodoState.CHECKED, TodoState.TODO));
    }

    @Test
    void aCleanAscentWinsOverAMarkByHand() {
        assertThat(TodoStates.of(Set.of(TOP_ROPE), Set.of(TOP_ROPE)))
                .isEqualTo(new TodoStates(TodoState.FROM_ASCENT, TodoState.TODO));
    }

    @Test
    void knownInLeadMeansKnownInTopRope() {
        assertThat(TodoStates.of(Set.of(LEAD), Set.of()))
                .isEqualTo(new TodoStates(TodoState.FROM_LEAD, TodoState.CHECKED));
        assertThat(TodoStates.of(Set.of(LEAD, TOP_ROPE), Set.of(LEAD, TOP_ROPE)))
                .isEqualTo(new TodoStates(TodoState.FROM_LEAD, TodoState.FROM_ASCENT));
    }

    @Test
    void aCleanAscentCountsEvenWithoutAMark() {
        assertThat(TodoStates.of(Set.of(), Set.of(LEAD)))
                .isEqualTo(new TodoStates(TodoState.FROM_LEAD, TodoState.FROM_ASCENT));
    }

    @Test
    void onlyTodoIsUnknown() {
        assertThat(TodoState.TODO.known()).isFalse();
        assertThat(TodoState.CHECKED.known()).isTrue();
        assertThat(TodoState.FROM_ASCENT.known()).isTrue();
        assertThat(TodoState.FROM_LEAD.known()).isTrue();
    }
}
