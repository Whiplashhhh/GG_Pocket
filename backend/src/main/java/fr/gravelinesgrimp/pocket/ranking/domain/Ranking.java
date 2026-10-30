package fr.gravelinesgrimp.pocket.ranking.domain;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Gives the ranks of a monthly ranking (spec §9.2): the highest score first; members on the same
 * score share a rank and the next rank skips as many places (1, 1, 3), then they are listed in the
 * alphabetical order of their nicknames, the way they are shown.
 */
public final class Ranking {

    private Ranking() {}

    public static List<RankedScore> of(List<Score> scores) {
        var collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.SECONDARY);
        var sorted = new ArrayList<>(scores);
        sorted.removeIf(score -> score.value() < 1);
        sorted.sort(
                Comparator.comparingInt(Score::value)
                        .reversed()
                        .thenComparing(Score::nickname, collator)
                        .thenComparing(Score::memberId));
        var ranked = new ArrayList<RankedScore>(sorted.size());
        for (var i = 0; i < sorted.size(); i++) {
            var score = sorted.get(i);
            var rank =
                    i > 0 && sorted.get(i - 1).value() == score.value()
                            ? ranked.get(i - 1).rank()
                            : i + 1;
            ranked.add(new RankedScore(rank, score));
        }
        return List.copyOf(ranked);
    }
}
