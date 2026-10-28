package fr.gravelinesgrimp.pocket.session.domain;

import java.util.UUID;

/** A number of things (comments…) on one session, from a grouped query. */
public record SessionCount(UUID sessionId, Long count) {}
