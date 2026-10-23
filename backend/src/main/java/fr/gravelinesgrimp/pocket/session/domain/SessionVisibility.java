package fr.gravelinesgrimp.pocket.session.domain;

/** Who may read a session. Never rename a constant: stored in the database. */
public enum SessionVisibility {
    /** The climber and their friends (the default, spec §7). */
    FRIENDS,
    /** "Juste moi". */
    PRIVATE
}
