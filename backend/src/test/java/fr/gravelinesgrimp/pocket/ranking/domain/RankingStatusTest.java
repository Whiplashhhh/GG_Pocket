package fr.gravelinesgrimp.pocket.ranking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

class RankingStatusTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Test
    void thisMonthIsLive() {
        assertThat(status(OCTOBER, LocalDateTime.of(2026, 10, 30, 12, 0)))
                .isEqualTo(RankingStatus.LIVE);
    }

    @Test
    void lastMonthIsProvisionalUntilThe8thAt4() {
        assertThat(status(OCTOBER, LocalDateTime.of(2026, 11, 1, 0, 0)))
                .isEqualTo(RankingStatus.PROVISIONAL);
        assertThat(status(OCTOBER, LocalDateTime.of(2026, 11, 8, 3, 59)))
                .isEqualTo(RankingStatus.PROVISIONAL);
        assertThat(status(OCTOBER, LocalDateTime.of(2026, 11, 8, 4, 0)))
                .isEqualTo(RankingStatus.FROZEN);
    }

    @Test
    void olderMonthsAreFrozen() {
        assertThat(status(OCTOBER, LocalDateTime.of(2027, 1, 2, 12, 0)))
                .isEqualTo(RankingStatus.FROZEN);
    }

    private static RankingStatus status(YearMonth month, LocalDateTime parisTime) {
        var now = parisTime.atZone(ClubTime.ZONE).toInstant();
        return RankingStatus.of(month, now, parisTime.toLocalDate());
    }
}
