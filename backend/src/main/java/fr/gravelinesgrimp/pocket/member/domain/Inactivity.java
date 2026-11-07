package fr.gravelinesgrimp.pocket.member.domain;

import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import java.time.Instant;

/**
 * Inactive accounts (DECISIONS D-119): one year without a visit puts an account to sleep, two years
 * delete it. Years are calendar years in Gravelines: the same day and time, one or two years back.
 * Admins are never touched (there must always be one, and they run the club).
 */
public final class Inactivity {

    public static final int YEARS_BEFORE_SLEEP = 1;

    public static final int YEARS_BEFORE_DELETION = 2;

    /** What the daily job does with an account. */
    public enum Verdict {
        KEEP,
        PUT_TO_SLEEP,
        DELETE
    }

    private Inactivity() {}

    /**
     * The last sign of life: the last visit, or the registration for an account never seen since,
     * or the reactivation by the board if it is more recent (so the year starts over from there).
     */
    public static Instant lastVisit(Instant createdAt, Instant lastSeenAt, Instant reactivatedAt) {
        var last = createdAt;
        for (var candidate : new Instant[] {lastSeenAt, reactivatedAt}) {
            if (candidate != null && (last == null || candidate.isAfter(last))) {
                last = candidate;
            }
        }
        return last;
    }

    /**
     * Exactly one year after the last visit, the account is kept: it goes to sleep the moment
     * after. An account already disabled (asleep or by the board) is only ever deleted.
     */
    public static Verdict verdict(Role role, boolean active, Instant lastVisit, Instant now) {
        if (role == Role.ADMIN || lastVisit == null) {
            return Verdict.KEEP;
        }
        if (lastVisit.isBefore(yearsBack(now, YEARS_BEFORE_DELETION))) {
            return Verdict.DELETE;
        }
        if (active && lastVisit.isBefore(yearsBack(now, YEARS_BEFORE_SLEEP))) {
            return Verdict.PUT_TO_SLEEP;
        }
        return Verdict.KEEP;
    }

    static Instant yearsBack(Instant now, int years) {
        return now.atZone(ClubTime.ZONE).minusYears(years).toInstant();
    }
}
