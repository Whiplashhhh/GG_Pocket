package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.Arrays;
import java.util.List;

/**
 * A badge as the climbers see it: a tier family with its levels (one per tier earned), or a
 * repeatable badge counted ×N. The values are stored (favourites): never rename one.
 */
public enum Badge {
    TOPPED,
    LEAD,
    FIRST_GRADE,
    CLIMBING_DAYS,
    WEEK_2,
    WEEK_3,
    WEEK_4,
    MONTH_COMPLETE,
    MONTH_12,
    STREAK_3W,
    MONTH_NO_MISS,
    FULL_LEAD,
    PERFECT,
    ZERO_REST,
    COMEBACK,
    FLASH,
    RECORD;

    public static Badge of(BadgeCode code) {
        return valueOf(code.repeatable() ? code.name() : code.family().name());
    }

    /** Its tiers, lowest first, or its single repeatable code. */
    public List<BadgeCode> codes() {
        return Arrays.stream(BadgeCode.values()).filter(code -> of(code) == this).toList();
    }

    public boolean tiered() {
        return !codes().getFirst().repeatable();
    }
}
