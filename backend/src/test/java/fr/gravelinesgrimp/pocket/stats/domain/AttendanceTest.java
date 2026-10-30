package fr.gravelinesgrimp.pocket.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttendanceTest {

    @Test
    void theSuccessRateIsTheRoundedShareOfToppedAscents() {
        assertThat(new Attendance(3, 2, 0, 2).successPercent()).isEqualTo(67);
        assertThat(new Attendance(8, 1, 1, 3).successPercent()).isEqualTo(13);
        assertThat(new Attendance(4, 4, 1, 1).successPercent()).isEqualTo(100);
        assertThat(new Attendance(5, 0, 0, 5).successPercent()).isZero();
    }

    @Test
    void withoutAnyAscentThereIsNoSuccessRate() {
        assertThat(Attendance.NONE.successPercent()).isNull();
    }
}
