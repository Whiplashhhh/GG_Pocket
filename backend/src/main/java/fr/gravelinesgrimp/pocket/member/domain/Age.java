package fr.gravelinesgrimp.pocket.member.domain;

import java.time.LocalDate;
import java.time.Period;

/**
 * Age rules (spec §6, DECISIONS D-119). Under 15, a declared parental consent; under 18, a minor:
 * only the board and the admin know it, and the age is never shown to the other members.
 */
public final class Age {

    public static final int PARENTAL_CONSENT_AGE = 15;

    public static final int ADULT_AGE = 18;

    private Age() {}

    public static int yearsOn(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    public static boolean needsParentalConsent(LocalDate birthDate, LocalDate today) {
        return birthDate != null && yearsOn(birthDate, today) < PARENTAL_CONSENT_AGE;
    }

    /** False when the birth date is unknown (an older account that has not given it yet). */
    public static boolean isMinor(LocalDate birthDate, LocalDate today) {
        return birthDate != null && yearsOn(birthDate, today) < ADULT_AGE;
    }

    /**
     * The age the other members see: only an adult's, and only if they chose to show it, whatever
     * a minor once saved.
     */
    public static Integer shownToOthers(LocalDate birthDate, boolean showAge, LocalDate today) {
        if (!showAge || birthDate == null || isMinor(birthDate, today)) {
            return null;
        }
        return yearsOn(birthDate, today);
    }
}
