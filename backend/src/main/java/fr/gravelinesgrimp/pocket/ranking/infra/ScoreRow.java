package fr.gravelinesgrimp.pocket.ranking.infra;

import fr.gravelinesgrimp.pocket.ranking.domain.Score;

import java.util.UUID;

/**
 * A member's score for a month, as counted by the database: {@code value} whole points, plus {@code
 * halves} half points (routes past their half, D-121).
 */
public record ScoreRow(UUID memberId, String nickname, UUID avatarId, long value, long halves) {

    /** Whole points only: climbing days. */
    public ScoreRow(UUID memberId, String nickname, UUID avatarId, long value) {
        this(memberId, nickname, avatarId, value, 0);
    }

    public Score toScore() {
        return new Score(memberId, nickname, avatarId, Score.points(value, halves));
    }
}
