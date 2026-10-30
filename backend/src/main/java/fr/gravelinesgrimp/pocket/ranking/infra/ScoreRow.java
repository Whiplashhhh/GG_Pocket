package fr.gravelinesgrimp.pocket.ranking.infra;

import fr.gravelinesgrimp.pocket.ranking.domain.Score;

import java.util.UUID;

/** A member's score for a month, as counted by the database. */
public record ScoreRow(UUID memberId, String nickname, UUID avatarId, long value) {

    public Score toScore() {
        return new Score(memberId, nickname, avatarId, Math.toIntExact(value));
    }
}
