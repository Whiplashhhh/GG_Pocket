package fr.gravelinesgrimp.pocket.member.domain;

/**
 * The four club roles, from the least to the most powerful. Each role has the rights of the roles
 * below it (spec §5).
 */
public enum Role {
    /** "Grimpeur GG". */
    CLIMBER,
    /** "Ouvreur·se": opens and dismounts routes. */
    SETTER,
    /** "Direction": manages members and moderates. */
    BOARD,
    /** "Le Président·e": everything, including club settings and registration codes. */
    ADMIN;

    public boolean isAtLeast(Role other) {
        return compareTo(other) >= 0;
    }

    /**
     * Spring Security authority name ({@code ROLE_BOARD}…), hierarchy declared in SecurityConfig.
     */
    public String authority() {
        return "ROLE_" + name();
    }
}
