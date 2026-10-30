package fr.gravelinesgrimp.pocket.ranking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class RankingTest {

    @Test
    void theHighestScoreComesFirst() {
        var ranked = Ranking.of(List.of(score("léa", 3), score("max", 7), score("ana", 5)));

        assertThat(ranked)
                .extracting(line -> line.score().nickname())
                .containsExactly("max", "ana", "léa");
        assertThat(ranked).extracting(RankedScore::rank).containsExactly(1, 2, 3);
    }

    @Test
    void tiesShareARankAndTheNextOneSkips() {
        var ranked =
                Ranking.of(
                        List.of(
                                score("a", 4),
                                score("b", 4),
                                score("c", 2),
                                score("d", 4),
                                score("e", 1)));

        assertThat(ranked).extracting(RankedScore::rank).containsExactly(1, 1, 1, 4, 5);
    }

    @Test
    void tiesFollowTheAlphabetWhateverTheCaseAndAccents() {
        var ranked =
                Ranking.of(
                        List.of(
                                score("Zoé", 2),
                                score("émile", 2),
                                score("adam", 2),
                                score("Eva", 2)));

        assertThat(ranked)
                .extracting(line -> line.score().nickname())
                .containsExactly("adam", "émile", "Eva", "Zoé");
    }

    @Test
    void nobodyIsRankedWithoutAScore() {
        assertThat(Ranking.of(List.of(score("zero", 0)))).isEmpty();
        assertThat(Ranking.of(List.of())).isEmpty();
    }

    private static Score score(String nickname, int value) {
        return new Score(UUID.randomUUID(), nickname, null, value);
    }
}
