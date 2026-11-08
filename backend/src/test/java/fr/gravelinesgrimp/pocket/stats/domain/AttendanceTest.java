package fr.gravelinesgrimp.pocket.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttendanceTest {

    @Test
    void theSuccessRateIsTheRoundedShareOfCleanAscents() {
        assertThat(new Attendance(3, 3, 2, 2).successPercent()).isEqualTo(67);
        assertThat(new Attendance(8, 4, 1, 3).successPercent()).isEqualTo(13);
        assertThat(new Attendance(4, 4, 4, 1).successPercent()).isEqualTo(100);
    }

    @Test
    void aToppedAscentWithFallsIsNotASuccess() {
        assertThat(new Attendance(4, 4, 1, 1).successPercent()).isEqualTo(25);
        assertThat(new Attendance(5, 5, 0, 5).successPercent()).isZero();
    }

    @Test
    void withoutAnyAscentThereIsNoSuccessRate() {
        assertThat(Attendance.NONE.successPercent()).isNull();
    }
}
