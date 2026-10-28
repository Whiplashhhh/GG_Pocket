package fr.gravelinesgrimp.pocket.session.domain;

/**
 * The four reactions to a session (spec §8.2): 🔥 💪 👏 🦍, in this order. Never rename a constant:
 * stored in the database.
 */
public enum ReactionKind {
    FIRE,
    STRONG,
    CLAP,
    GORILLA
}
