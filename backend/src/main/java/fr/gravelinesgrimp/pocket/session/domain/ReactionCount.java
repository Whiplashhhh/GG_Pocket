package fr.gravelinesgrimp.pocket.session.domain;

import java.util.UUID;

/** How many members gave one reaction to one session, from a grouped query. */
public record ReactionCount(UUID sessionId, ReactionKind kind, Long count) {}
