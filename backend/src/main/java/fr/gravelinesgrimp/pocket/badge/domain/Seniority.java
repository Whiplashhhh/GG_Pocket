package fr.gravelinesgrimp.pocket.badge.domain;

import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import java.time.Clock;

/**
 * The seniority badge (spec §9.4, D-008): years at the club = this year − the year of arrival, in
 * Gravelines time. Shown on the profile and in the directory.
 */
public enum Seniority {
    /** "Tout frais": arrived this year. */
    FRESH(0),
    /** "Le bébé mousqueton". */
    BABY_CARABINER(1),
    /** "Le débutant professionnel". */
    PRO_BEGINNER(2),
    /** "Le forceur". */
    FORCER(5),
    /** "Le résident de la salle". */
    RESIDENT(10),
    /** "Le GG fossile (il fait partie du mur)". */
    FOSSIL(15);

    private final int fromYears;

    Seniority(int fromYears) {
        this.fromYears = fromYears;
    }

    /** Today's seniority: the year changes at midnight in Gravelines, not in UTC. */
    public static Seniority today(Integer arrivalYear, Clock clock) {
        return of(arrivalYear, ClubTime.today(clock).getYear());
    }

    /** Null when the year of arrival is unknown. A year ahead of today counts as this year. */
    public static Seniority of(Integer arrivalYear, int currentYear) {
        if (arrivalYear == null) {
            return null;
        }
        var years = currentYear - arrivalYear;
        var seniority = FRESH;
        for (var candidate : values()) {
            if (years >= candidate.fromYears) {
                seniority = candidate;
            }
        }
        return seniority;
    }
}
