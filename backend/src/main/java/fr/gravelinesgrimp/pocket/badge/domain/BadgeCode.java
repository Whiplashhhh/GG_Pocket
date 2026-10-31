package fr.gravelinesgrimp.pocket.badge.domain;

import java.util.Arrays;
import java.util.List;

/**
 * The badge catalogue (spec §9.5, D-004 to D-006). The codes are stored: never rename one. Names,
 * conditions and icons live in the app, keyed by code; the order here is the library's.
 */
public enum BadgeCode {
    TOPPED_1(BadgeFamily.TOPPED, 1),
    TOPPED_5(BadgeFamily.TOPPED, 5),
    TOPPED_10(BadgeFamily.TOPPED, 10),
    TOPPED_20(BadgeFamily.TOPPED, 20),
    TOPPED_50(BadgeFamily.TOPPED, 50),
    TOPPED_100(BadgeFamily.TOPPED, 100),
    TOPPED_250(BadgeFamily.TOPPED, 250),
    TOPPED_500(BadgeFamily.TOPPED, 500),
    TOPPED_750(BadgeFamily.TOPPED, 750),
    TOPPED_1000(BadgeFamily.TOPPED, 1000),
    LEAD_1(BadgeFamily.LEAD, 1),
    LEAD_5(BadgeFamily.LEAD, 5),
    LEAD_10(BadgeFamily.LEAD, 10),
    LEAD_20(BadgeFamily.LEAD, 20),
    LEAD_50(BadgeFamily.LEAD, 50),
    LEAD_100(BadgeFamily.LEAD, 100),
    LEAD_250(BadgeFamily.LEAD, 250),
    LEAD_500(BadgeFamily.LEAD, 500),
    LEAD_750(BadgeFamily.LEAD, 750),
    LEAD_1000(BadgeFamily.LEAD, 1000),
    GRADE_5B(BadgeFamily.FIRST_GRADE, "5b"),
    GRADE_5C(BadgeFamily.FIRST_GRADE, "5c"),
    GRADE_6A(BadgeFamily.FIRST_GRADE, "6a"),
    GRADE_6B(BadgeFamily.FIRST_GRADE, "6b"),
    GRADE_6C(BadgeFamily.FIRST_GRADE, "6c"),
    GRADE_7A(BadgeFamily.FIRST_GRADE, "7a"),
    GRADE_7B(BadgeFamily.FIRST_GRADE, "7b"),
    GRADE_7C(BadgeFamily.FIRST_GRADE, "7c"),
    DAYS_10(BadgeFamily.CLIMBING_DAYS, 10),
    DAYS_25(BadgeFamily.CLIMBING_DAYS, 25),
    DAYS_50(BadgeFamily.CLIMBING_DAYS, 50),
    DAYS_75(BadgeFamily.CLIMBING_DAYS, 75),
    DAYS_100(BadgeFamily.CLIMBING_DAYS, 100),
    DAYS_500(BadgeFamily.CLIMBING_DAYS, 500),
    DAYS_750(BadgeFamily.CLIMBING_DAYS, 750),
    DAYS_1000(BadgeFamily.CLIMBING_DAYS, 1000),
    /** An ISO week with at least 2 climbing days. */
    WEEK_2(BadgeFamily.REPEATABLE, 2),
    /** An ISO week with at least 3 climbing days. */
    WEEK_3(BadgeFamily.REPEATABLE, 3),
    /** An ISO week with at least 4 climbing days. */
    WEEK_4(BadgeFamily.REPEATABLE, 4),
    /** "Mois complet": a month with at least 10 climbing days (D-005). */
    MONTH_COMPLETE(BadgeFamily.REPEATABLE, 10),
    /** A month with at least 12 climbing days. */
    MONTH_12(BadgeFamily.REPEATABLE, 12),
    /** 3 ISO weeks in a row with at least 2 climbing days each, blocks without overlap. */
    STREAK_3W(BadgeFamily.REPEATABLE, 3),
    /** A month whose every ISO week starting in it has a climbing day. */
    MONTH_NO_MISS(BadgeFamily.REPEATABLE, 1),
    /** A session of at least 3 ascents, all in lead. */
    FULL_LEAD(BadgeFamily.REPEATABLE, 3),
    /** A session of at least 3 ascents, all clean. */
    PERFECT(BadgeFamily.REPEATABLE, 3),
    /** A session with at least 10 topped ascents. */
    ZERO_REST(BadgeFamily.REPEATABLE, 10),
    /** A climbing day at least 30 days after the previous one (not the very first). */
    COMEBACK(BadgeFamily.REPEATABLE, 30),
    /** "Le flash" (D-006): a clean first try on a route opened at most 14 days before. */
    FLASH(BadgeFamily.REPEATABLE, 14),
    /** More topped ascents than any earlier session, from the 4th session on. */
    RECORD(BadgeFamily.REPEATABLE, 4);

    private final BadgeFamily family;
    private final int threshold;
    private final String grade;

    BadgeCode(BadgeFamily family, int threshold) {
        this.family = family;
        this.threshold = threshold;
        this.grade = null;
    }

    BadgeCode(BadgeFamily family, String grade) {
        this.family = family;
        this.threshold = GradeScale.rank(grade);
        this.grade = grade;
    }

    public BadgeFamily family() {
        return family;
    }

    public boolean repeatable() {
        return family == BadgeFamily.REPEATABLE;
    }

    /**
     * The count, number of days or rank this badge waits for; for {@link BadgeFamily#FIRST_GRADE},
     * the {@link GradeScale} rank of {@link #grade()}.
     */
    public int threshold() {
        return threshold;
    }

    /** The grade of a {@link BadgeFamily#FIRST_GRADE} tier ("6a"), otherwise null. */
    public String grade() {
        return grade;
    }

    /** The tiers of a family, lowest first. */
    public static List<BadgeCode> tiers(BadgeFamily family) {
        return Arrays.stream(values()).filter(code -> code.family == family).toList();
    }
}
