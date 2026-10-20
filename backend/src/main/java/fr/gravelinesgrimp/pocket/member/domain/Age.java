package fr.gravelinesgrimp.pocket.member.domain;

import java.time.LocalDate;
import java.time.Period;

/** Age rules. Minors under 15 need a declared parental consent (spec §6, DECISIONS D-014). */
public final class Age {

    public static final int PARENTAL_CONSENT_AGE = 15;

    private Age() {}

    public static int yearsOn(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    public static boolean needsParentalConsent(LocalDate birthDate, LocalDate today) {
        return birthDate != null && yearsOn(birthDate, today) < PARENTAL_CONSENT_AGE;
    }
}
