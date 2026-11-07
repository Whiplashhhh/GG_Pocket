package fr.gravelinesgrimp.pocket.ranking.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What a member scored in a month's ranking, before ranks are given. The "Voies" ranking keeps
 * halves (D-121): a route past its half is worth 0.5. The value is kept without trailing zeros (2,
 * not 2.0), so that equal scores are equal and a whole score reads as one.
 */
public record Score(UUID memberId, String nickname, UUID avatarId, BigDecimal value) {

    private static final BigDecimal HALF = new BigDecimal("0.5");

    public Score {
        value = plain(value);
    }

    /** A whole score: climbing days, or topped ascents alone. */
    public Score(UUID memberId, String nickname, UUID avatarId, long value) {
        this(memberId, nickname, avatarId, BigDecimal.valueOf(value));
    }

    /**
     * Whole points and half points: in the "Voies" ranking, one per topped ascent and one half per
     * route past its half (D-003, D-121).
     */
    public static BigDecimal points(long whole, long halves) {
        return BigDecimal.valueOf(whole).add(HALF.multiply(BigDecimal.valueOf(halves)));
    }

    /** 2.0 becomes 2, 12.50 becomes 12.5 (never 1E+1). */
    public static BigDecimal plain(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
