package fr.gravelinesgrimp.pocket.ranking.infra;

import fr.gravelinesgrimp.pocket.ranking.domain.RankedScore;
import fr.gravelinesgrimp.pocket.ranking.domain.RankingKind;
import fr.gravelinesgrimp.pocket.ranking.domain.Score;

import java.util.UUID;

/** A frozen rank, with who holds it today. */
public record FrozenRow(
        RankingKind kind, int rank, int value, UUID memberId, String nickname, UUID avatarId) {

    public RankedScore toRankedScore() {
        return new RankedScore(rank, new Score(memberId, nickname, avatarId, value));
    }
}
