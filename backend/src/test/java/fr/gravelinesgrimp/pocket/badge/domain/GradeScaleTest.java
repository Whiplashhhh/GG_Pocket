package fr.gravelinesgrimp.pocket.badge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

import java.util.List;

class GradeScaleTest {

    @Test
    void followsTheClubGrades() {
        var grades =
                List.of("4a-", "4a", "4a+", "4c+", "5a-", "6a-", "6a", "6a+", "6b-", "7c", "7c+");

        assertThat(grades.stream().map(GradeScale::rank).toList())
                .isSorted()
                .doesNotHaveDuplicates();
    }

    @Test
    void theBaseDropsTheModifier() {
        assertThat(List.of("6a-", "6a", "6a+").stream().map(GradeScale::base).distinct())
                .containsExactly("6a");
    }

    @Test
    void refusesWhatIsNotAGrade() {
        assertThatIllegalArgumentException().isThrownBy(() -> GradeScale.rank("6d"));
        assertThatIllegalArgumentException().isThrownBy(() -> GradeScale.rank("6A"));
        assertThatIllegalArgumentException().isThrownBy(() -> GradeScale.base("6d"));
    }
}
